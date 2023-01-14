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
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pl.umk.mat.zesp01.pz2022.researcher.model.LoginData
import pl.umk.mat.zesp01.pz2022.researcher.model.Token
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.TokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.Date
import java.util.concurrent.TimeUnit

val ACCESS_TOKEN_SECRET : String = System.getenv("ACCESS_TOKEN_SECRET")
val REFRESH_TOKEN_SECRET : String = System.getenv("REFRESH_TOKEN_SECRET")

@RestController
class AuthController(@Autowired val userService: UserService, @Autowired val tokenService: TokenService) {

    val gson = Gson()

    @PostMapping("/refreshToken")
    fun checkRefreshToken(
        @CookieValue(name = "jwt") token: String,
        @RequestBody login : String
    ):ResponseEntity<String>{

        val userTokens = tokenService.getTokensByLogin(login)

        userTokens.filter{item: Token -> (item.jwt==token)}


        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PostMapping("/auth")
    fun checkUserDetails(@RequestBody loginData: LoginData): ResponseEntity<String> {
            val user = userService.getUserByLogin(loginData.login)
            if (user != User() ){
                if(BCrypt.checkpw(loginData.password,user.password)) try {
                    val payload = mapOf(Pair("username",user.login))
                    val refreshExpires = TimeUnit.DAYS.toMillis(1)
                    val accessExpires = TimeUnit.MINUTES.toMillis(10)

                    //CREATE JWTs
                    val accessToken = JWT
                        .create()
                        .withPayload(payload)
                        .withExpiresAt(Date(System.currentTimeMillis() + accessExpires))
                        .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))

                    val refreshToken = JWT
                        .create()
                        .withPayload(payload)
                        .withExpiresAt(Date(System.currentTimeMillis() + refreshExpires))
                        .sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))

                    val cookie = ResponseCookie
                        .from("jwt",refreshToken)
                        .httpOnly(true)
                        .maxAge(TimeUnit.DAYS.toSeconds(1))
                        .path("/")
                        .build()

                    //CREATE TOKEN DOCUMENT
                    val refreshTokenDB = Token()

                    refreshTokenDB.login = user.login
                    refreshTokenDB.expires = Date(System.currentTimeMillis() + refreshExpires).toString()
                    refreshTokenDB.jwt = refreshToken

                    //ADD TOKEN DOCUMENT TO DATABASE
                    tokenService.addToken(refreshTokenDB)

                    return ResponseEntity
                        .status(HttpStatus.OK)
                        .header(HttpHeaders.SET_COOKIE,cookie.toString())
                        .body(accessToken)

                }catch (error:Error){
                    return ResponseEntity.status(HttpStatus.OK).body(gson.toJson("Failed creating a token."))
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson("Login failed: wrong password."))
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(gson.toJson("Login failed: user does not exist."))
        }
}