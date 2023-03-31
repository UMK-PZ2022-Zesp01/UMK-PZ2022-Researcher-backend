package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.LoginData

import pl.umk.mat.zesp01.pz2022.researcher.service.REFRESH_EXPIRES_SEC
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService


@RestController
class AuthController(@Autowired val userService: UserService, @Autowired val refreshTokenService: RefreshTokenService) {

    /*** POST MAPPINGS ***/

    @PostMapping("/login")
    fun handleLogin(@RequestBody loginData: LoginData): ResponseEntity<String> {
        val user = userService.getUserByLogin(loginData.login).orElse(null)

        user ?: return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Login failed: user does not exist.")

        if (!BCrypt.checkpw(loginData.password, user.password)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Login failed: user does not exist.")
        }
        if (!user.isConfirmed) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Login failed: account has not been activated.")
        }

        try {
            val username = user.login

            //CREATE JWTs
            val accessToken = refreshTokenService
                .createAccessToken(username)

            val refreshToken = refreshTokenService
                .createRefreshToken(username)

            //CREATE REFRESH TOKEN COOKIE
            val cookie = ResponseCookie
                .from("jwt", refreshToken)
                .httpOnly(true)
                .maxAge(REFRESH_EXPIRES_SEC)
                .path("/")
                .sameSite("none") //Chrome, you bastard
                .secure(true)
                .build()

            //CREATE RESPONSE BODY

            val responseBody = HashMap<String, String>()
            responseBody["username"] = user.login
            responseBody["accessToken"] = accessToken

            //SEND THE REFRESH TOKEN COOKIE AND THE ACCESS TOKEN
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Gson().toJson(responseBody))

        } catch (error: Exception) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Something went wrong, please try again")
        }
    }

    /*** GET MAPPINGS ***/

    @GetMapping("/auth/refresh")
    fun handleRefreshToken(@CookieValue(name = "jwt") jwt: String): ResponseEntity<String> {
        fun deleteCookie(): ResponseEntity<String> {
            val deleteCookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("none") //Chrome, you bastard
                .secure(true)
                .build()

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build()
        }

        //Provided token is in the database - his owner logged out before it expired, and it's blacklisted.
        val bannedToken = refreshTokenService.getTokenByJwt(jwt)
        if (bannedToken.isPresent) return deleteCookie()

        //Provided token does not have proper payload, or the user does not exist.
        val username = refreshTokenService.verifyRefreshToken(jwt)
        if (username.isNullOrEmpty()) return deleteCookie()
        val user = userService.getUserByLogin(username)
        if (user.isEmpty) return deleteCookie()


        //Create a new access token for the user.
        val accessToken = refreshTokenService.createAccessToken(username)

        val responseBody = HashMap<String, String>()
        responseBody["username"] = username
        responseBody["accessToken"] = accessToken

        return ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(responseBody))
    }

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/logout")
    fun handleLogout(@CookieValue(name = "jwt") jwt: String): ResponseEntity<String> {
        try {
            val bannedRefreshToken = refreshTokenService.addToken(jwt)

            //Cookie deleter
            val deleteCookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("none") //Chrome, you bastard
                .secure(true)
                .build()

            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build()

        } catch (e: Exception) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        }
    }
}