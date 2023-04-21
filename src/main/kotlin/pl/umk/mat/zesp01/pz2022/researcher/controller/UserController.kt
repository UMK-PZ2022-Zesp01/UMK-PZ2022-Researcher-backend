package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pl.umk.mat.zesp01.pz2022.researcher.model.UserRegisterRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@RestController
class UserController(
	@Autowired val userService: UserService,
	@Autowired val verificationTokenService: VerificationTokenService,
	@Autowired val refreshTokenService: RefreshTokenService,
	@Autowired val eventPublisher: ApplicationEventPublisher,
	@Autowired val researchService: ResearchService
) {

	@PostMapping("/user/register")
	fun addUser(@RequestBody userRegisterRequest: UserRegisterRequest): ResponseEntity<String> {
		if (userService.isEmailAlreadyTaken(userRegisterRequest.email))
			return ResponseEntity.status(299).build()

		if (userService.isLoginAlreadyTaken(userRegisterRequest.login))
			return ResponseEntity.status(298).build()

		val newUser = userRegisterRequest.toUser()
		userService.addUser(newUser)
		return ResponseEntity.status(HttpStatus.CREATED).build()
	}

	@GetMapping("/user/sendVerificationMail")
	fun sendVerificationEmail(@RequestParam("login") login: String): ResponseEntity<String> {
		return try {
			val user = userService.getUserByLogin(login).get()
			if (user.isConfirmed) throw (Exception())

			verificationTokenService.deleteUserTokens(user)
			eventPublisher.publishEvent(OnRegistrationCompleteEvent(user))

			ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(user.email))
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}
	}

	@GetMapping("/user/confirm")
	fun confirmAccount(@RequestParam("token") token: String): ResponseEntity<String> {
		try {
			val verificationToken = verificationTokenService.getTokenByJwt(token).get()
			val user = userService.getUserByLogin(verificationToken.login).get()

			if (user.isConfirmed)
				return ResponseEntity.status(HttpStatus.NO_CONTENT).build()

			val tokenOwner = verificationTokenService.verifyToken(verificationToken.jwt)
			if (tokenOwner != user.login) throw (Exception())

			userService.activateUserAccount(user)
			verificationTokenService.deleteUserTokens(user)

			return ResponseEntity.status(HttpStatus.CREATED).build()
		} catch (e: Exception) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}
	}

	@GetMapping("/user/{login}")
	fun getUserByLogin(@PathVariable login: String): ResponseEntity<String> =
		try {
			ResponseEntity.status(HttpStatus.OK).body(
				Gson().toJson(userService.getUserByLogin(login).get().toUserResponse())
			)
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}

	@GetMapping("/user/current", produces = ["application/json;charset:UTF-8"])
	fun getCurrentUser(@RequestHeader httpHeaders: HttpHeaders): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		try {
			/** Get the username claimed in the access token **/
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()

			/** If claimed user does not exist in db there is something wrong with the token **/
			val user = userService.getUserByLogin(username)
			if (user.isEmpty) throw Exception()

			val data = userService
				.getUserByLogin(username)
				.get()
				.toUserResponse()

			return ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(data))
		} catch (e: Exception) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}

	@PutMapping("/user/current/update", produces = ["application/json;charset:UTF-8"])
	fun updateCurrentUser(
		@RequestHeader httpHeaders: HttpHeaders,
		@RequestBody userUpdateData: UserUpdateRequest
	): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()
			val user = userService.getUserByLogin(username).get()

			val updateResult = userService.updateUser(user, userUpdateData)

			if (updateResult == "phone") return ResponseEntity.status(299).build()
			if (updateResult == "email") return ResponseEntity.status(298).build()
			return ResponseEntity.status(HttpStatus.OK).build()
		} catch (e: Exception) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}

	@PutMapping("/user/current/avatar/update", consumes = ["multipart/form-data"])
	fun updateCurrentUserAvatar(
		@RequestHeader httpHeaders: HttpHeaders,
		@RequestPart ("userAvatar") userAvatar : MultipartFile,
	): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		return try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()
			val user = userService.getUserByLogin(username).get()
			userService.updateUserAvatar(user,userAvatar)
			ResponseEntity.status(HttpStatus.OK).build()
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}

	@DeleteMapping("/user/current/delete")
	fun deleteCurrentUser(@RequestHeader httpHeaders: HttpHeaders): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		return try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()

			// Delete User from All Researches
			researchService.removeUserFromAllResearches(username)

			userService.deleteUserByLogin(username)
			ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}
}