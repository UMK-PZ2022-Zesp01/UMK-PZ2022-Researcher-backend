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

    val gson = Gson()

    /*** POST MAPPINGS ***/

    @PostMapping("/login")
    fun handleLogin(@RequestBody loginData: LoginData): ResponseEntity<String> {
        val user = userService.getUserByLogin(loginData.login)

        if ( user.isEmpty){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: user does not exist.")
        }

        if (!BCrypt.checkpw(loginData.password, user.get().password)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: user does not exist.")
        }

        if(!user.get().isConfirmed){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login failed: account has not been activated.")
        }

        try {
            val username= user.get().login

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
            responseBody["username"] = user.get().login
            responseBody["accessToken"] = accessToken

            //SEND THE REFRESH TOKEN COOKIE AND THE ACCESS TOKEN
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(gson.toJson(responseBody))

        } catch (error: Exception) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Something went wrong, please try again")
        }

    }

    /*** GET MAPPINGS ***/

    @GetMapping("/auth/refresh")
    fun handleRefreshToken(
        @CookieValue(name = "jwt") jwt: String
    ): ResponseEntity<String> {
        val token = refreshTokenService.getTokenByJwt(jwt)

        if (token.isEmpty) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val username= token.get().login
        if(!refreshTokenService.verifyRefreshToken(jwt,username)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val accessToken = refreshTokenService
            .createAccessToken(username)

        val responseBody = HashMap<String, String>()
            responseBody["username"] = token.get().login
            responseBody["accessToken"] = accessToken

        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(responseBody))
    }

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/logout")
    fun handleLogout(
        @CookieValue(name = "jwt") jwt: String
    ): ResponseEntity<String> {
        val token = refreshTokenService.getTokenByJwt(jwt)
        if (token.isPresent) {//The token is in the db. Delete it
            refreshTokenService.deleteToken(token.get().id)
        }
        //Delete the cookie
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
    }
}