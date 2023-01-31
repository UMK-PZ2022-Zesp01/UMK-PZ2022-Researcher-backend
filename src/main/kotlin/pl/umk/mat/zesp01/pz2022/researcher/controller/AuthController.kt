package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.LoginData
import pl.umk.mat.zesp01.pz2022.researcher.model.Token
import pl.umk.mat.zesp01.pz2022.researcher.service.TokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.Date
import java.util.StringJoiner

val ACCESS_TOKEN_SECRET: String = System.getenv("ACCESS_TOKEN_SECRET")
val REFRESH_TOKEN_SECRET: String = System.getenv("REFRESH_TOKEN_SECRET")
const val ACCESS_EXPIRES_SEC: Long = 600
const val REFRESH_EXPIRES_SEC: Long = 86400

@RestController
class AuthController(@Autowired val userService: UserService, @Autowired val tokenService: TokenService) {

    val gson = Gson()

    /*** POST MAPPINGS ***/

    @PostMapping("/login")
    fun handleLogin(@RequestBody loginData: LoginData): ResponseEntity<String> {
        val user = userService.getUserByLogin(loginData.login)
        if (user.isPresent) {
            if (BCrypt.checkpw(loginData.password, user.get().password))
                try {
                    val payload = mapOf(Pair("username", user.get().login))

                    //CREATE JWTs
                    val accessToken = JWT
                        .create()
                        .withPayload(payload)
                        .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
                        .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))

                    val refreshToken = JWT
                        .create()
                        .withPayload(payload)
                        .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000))
                        .sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))

                    //CREATE TOKEN DOCUMENT
                    val refreshTokenDB = Token()

                    refreshTokenDB.login = user.get().login
                    refreshTokenDB.expires = Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000).toString()
                    refreshTokenDB.jwt = refreshToken

                    //ADD TOKEN DOCUMENT TO DATABASE
                    tokenService.addToken(refreshTokenDB)

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
                        .status(HttpStatus.OK)
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(gson.toJson(responseBody))

                } catch (error: Exception) {
                    return ResponseEntity.status(HttpStatus.OK).body("Failed creating a token.")
                }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: wrong password.")
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: user does not exist.")
    }

    /*** GET MAPPINGS ***/

    @GetMapping("/refreshAccess")
    fun handleRefreshToken(
        @CookieValue(name = "jwt") jwt: String
    ): ResponseEntity<String> {
        val token = tokenService.getTokenByJwt(jwt)
        if (token.isEmpty) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        return try {
            //Verify if refresh token is valid. If it's not, it will throw an exception
            JWT.require(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
                .withClaim("username", token.get().login)
                .build()
                .verify(jwt)

            //If refresh token is valid, create a new access token
            val payload = mapOf(Pair("username", token.get().login))

            val accessToken = JWT
                .create()
                .withPayload(payload)
                .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
                .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))

            val responseBody = HashMap<String, String>()
            responseBody["username"] = token.get().login
            responseBody["accessToken"] = accessToken

            ResponseEntity.status(HttpStatus.OK).body(gson.toJson(responseBody))
        } catch (error: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/logout")
    fun handleLogout(
        @CookieValue(name = "jwt") jwt: String
    ): ResponseEntity<String> {
        val token = tokenService.getTokenByJwt(jwt)
        if (token.isPresent) {//The token is in the db. Delete it
            tokenService.deleteToken(token.get().id)
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