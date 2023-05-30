package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.GoogleLoginRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.LoginData
import pl.umk.mat.zesp01.pz2022.researcher.service.ACCESS_EXPIRES_SEC
import pl.umk.mat.zesp01.pz2022.researcher.service.REFRESH_EXPIRES_SEC
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import kotlin.Exception
import kotlin.String


@RestController
class AuthController(
    @Autowired val userService: UserService,
    @Autowired val refreshTokenService: RefreshTokenService
) {

    @PostMapping("/login")
    fun handleLogin(@RequestBody loginData: LoginData): ResponseEntity<String> {
        val user = userService.getUserByLogin(loginData.login).orElse(userService.userRepository.findUserByEmail(loginData.login).orElse(null))
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Login failed: User ${loginData.login} does not exist")

        if (!BCrypt.checkpw(loginData.password, user.password)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Login failed: Wrong password")
        }
        if (!user.isConfirmed) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Login failed: Account has not been activated")
        }

        try {
            val username = user.login

            /** Create access token **/
            val accessToken = refreshTokenService
                .createAccessToken(username)

            /** Create Response Body **/
            val responseBody = HashMap<String, String>()
            responseBody["username"] = user.login
            responseBody["accessToken"] = accessToken
            responseBody["locationSet"] = user.location.isNotEmpty().toString()

            /** Create refresh token **/
            val tokenDuration = if (loginData.rememberDevice) {REFRESH_EXPIRES_SEC} else{ACCESS_EXPIRES_SEC}
            val refreshToken = refreshTokenService.createRefreshToken(username, tokenDuration)
            if (refreshToken.isNullOrEmpty()) throw Exception()

            /** Create Refresh Token Cookie **/
            val cookie = ResponseCookie
                .from("jwt", refreshToken)
                .httpOnly(true)
                .maxAge(tokenDuration+7200)
                .path("/")
                .sameSite("none") //Chrome, you bastard
                .secure(true)
                .build()

            /** Send Refresh Token Cookie & Access Token **/
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Gson().toJson(responseBody))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Something went wrong, please try again")
        }
    }



    @PostMapping("/google/login")
    fun handleLogin(@RequestBody googleLoginRequest: GoogleLoginRequest): ResponseEntity<String> {
        try {
            if (googleLoginRequest.email==null)
                throw Exception()
            val transport = NetHttpTransport()
            val jsonFactory: JsonFactory = GsonFactory()
            val verifier = GoogleIdTokenVerifier.Builder(
                transport,
                jsonFactory
            )
                .setAudience(listOf(System.getenv("CLIENT_ID")))
                .build()

            val idToken: GoogleIdToken = verifier.verify(googleLoginRequest.jwt) ?: throw Exception()
            val payload: GoogleIdToken.Payload = idToken.payload

            val user = userService.userRepository.findUserByEmail(googleLoginRequest.email).orElse(null)
                ?:throw Exception()

            val accessToken = refreshTokenService.createAccessToken(user.login)

            /** Create Response Body **/
            val responseBody = HashMap<String, String>()
            responseBody["username"] = user.login
            responseBody["accessToken"] = accessToken
            responseBody["locationSet"] = user.location.isNotEmpty().toString()

            /** Create refresh token **/
            val tokenDuration = ACCESS_EXPIRES_SEC
            val refreshToken = refreshTokenService.createRefreshToken(user.login, tokenDuration)
            if (refreshToken.isNullOrEmpty()) throw Exception()

            /** Create Refresh Token Cookie **/
            val cookie = ResponseCookie
                .from("jwt", refreshToken)
                .httpOnly(true)
                .maxAge(tokenDuration+7200)
                .path("/")
                .sameSite("none") //Chrome, you bastard
                .secure(true)
                .build()

            /** Send Refresh Token Cookie & Access Token **/
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Gson().toJson(responseBody))

//            val userId = payload.subject
//            val email = payload.email
//            val emailVerified = Boolean.valueOf(payload.emailVerified)
//            val name = payload["name"] as String?
//            val pictureUrl = payload["picture"] as String?
//            val locale = payload["locale"] as String?
//            val familyName = payload["family_name"] as String?
//            val givenName = payload["given_name"] as String?
//            return ResponseEntity.status(HttpStatus.OK).body("OK")

        }catch (_:Exception){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Something went wrong, please try again")
        }
    }

    @GetMapping("/auth/refresh")
    fun handleRefreshToken(@CookieValue(name = "jwt") jwt: String): ResponseEntity<String> {

        try {
            /** Check if provided token is in the database.
            Check if it's valid.
            Get token's owner. **/
            val token = refreshTokenService.verifyRefreshToken(jwt) ?: throw Exception()
            val username = token.username

            /** Check if user mentioned in the payload is in the database. **/
            val user = userService.getUserByLogin(username).orElseThrow()

            /** Create a new access token for the user and send it. **/
            val accessToken = refreshTokenService.createAccessToken(username)

            /** Prepare session data. **/
            val responseBody = HashMap<String, String>()
            responseBody["username"] = username
            responseBody["accessToken"] = accessToken
            responseBody["locationSet"] = user.location.isNotEmpty().toString()

            return ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(responseBody))
        } catch (e: Exception) {
            val deleteCookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("none") //Chrome, you bastard.
                .secure(true)
                .build()


            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build()
        }
    }

    @DeleteMapping("/logout")
    fun handleLogout(@CookieValue(name = "jwt") plainJwt: String): ResponseEntity<String> {
        try {
            /** Delete refresh token from the database.
            Prevent logout on fail. **/
            if (!refreshTokenService.removeRefreshToken(plainJwt)) throw Exception()

            /** Prepare the cookie deleter. **/
            val deleteCookie = ResponseCookie
                .from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("none") //Chrome, you bastard.
                .secure(true)
                .build()

            /** Logout **/
            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build()

        } catch (e: Exception) {
            /** Prevent logout **/
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        }
    }
}