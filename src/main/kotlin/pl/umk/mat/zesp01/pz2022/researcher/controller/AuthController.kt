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
class AuthController(
	@Autowired val userService: UserService,
	@Autowired val refreshTokenService: RefreshTokenService
) {

	@PostMapping("/login")
	fun handleLogin(@RequestBody loginData: LoginData): ResponseEntity<String> {
		val user = userService.getUserByLogin(loginData.login).orElse(null)
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

			/** Create JWTs **/
			val accessToken = refreshTokenService
				.createAccessToken(username)

			val refreshToken = refreshTokenService
				.createRefreshToken(username)

			/** Create Refresh Token Cookie **/
			val cookie = ResponseCookie
				.from("jwt", refreshToken)
				.httpOnly(true)
				.maxAge(REFRESH_EXPIRES_SEC)
				.path("/")
				.sameSite("none") //Chrome, you bastard
				.secure(true)
				.build()

			/** Create Response Body **/
			val responseBody = HashMap<String, String>()
			responseBody["username"] = user.login
			responseBody["accessToken"] = accessToken

			/** Send Refresh Token Cookie & Access Token **/
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(Gson().toJson(responseBody))
		} catch (e: Exception) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Something went wrong, please try again")
		}
	}

	@GetMapping("/auth/refresh")
	fun handleRefreshToken(@CookieValue(name = "jwt") jwt: String): ResponseEntity<String> {

		try {
			/** Check if provided token is in the database
			It would mean his owner logged out before it expired, and the token is now blacklisted. **/
			val bannedToken = refreshTokenService.getTokenByJwt(jwt)
			if (bannedToken.isPresent) throw Exception()

			/** Check if provided token has proper payload. **/
			val username = refreshTokenService.verifyRefreshToken(jwt)
			if (username.isNullOrEmpty()) throw Exception()

			/** Check if user mentioned in the payload is in the database. **/
			val user = userService.getUserByLogin(username)
			if (user.isEmpty) throw Exception()

			/** Create a new access token for the user and send it. **/
			val accessToken = refreshTokenService.createAccessToken(username)

			val responseBody = HashMap<String, String>()
			responseBody["username"] = username
			responseBody["accessToken"] = accessToken

			return ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(responseBody))
		} catch (e: Exception) {
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
	}

	@DeleteMapping("/logout")
	fun handleLogout(@CookieValue(name = "jwt") jwt: String): ResponseEntity<String> {
		try {
			/** Add the token to blacklist, as it is not expired yet. **/
			refreshTokenService.addToken(jwt)

			/** Prepare the cookie deleter **/
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