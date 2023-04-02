package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import pl.umk.mat.zesp01.pz2022.researcher.model.UserRegisterRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@RestController
class UserController(
	@Autowired val userService: UserService,
	@Autowired val verificationTokenService: VerificationTokenService,
	@Autowired val refreshTokenService: RefreshTokenService,
	@Autowired val eventPublisher: ApplicationEventPublisher,
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

	@PutMapping("/user/{login}/update")
	fun updateUser(
		@PathVariable login: String,
		@RequestBody userUpdateData: UserUpdateRequest
	): ResponseEntity<User> {
		val user = userService.getUserByLogin(login).get()
		userService.updateUser(user, userUpdateData)
		return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByLogin(login).get())
	}

	@GetMapping("/user/current")
	fun getUserProfile(@RequestHeader httpHeaders: HttpHeaders): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		try {
			//get the username claimed in the access token
			val username = refreshTokenService.verifyAccessToken(jwt[0])
			if (username.isNullOrEmpty()) throw Exception()

			//If claimed user does not exist in db there is something wrong with the token
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

//	@GetMapping("/getPhoneByUserLogin/{login}")
//	fun getPhoneByUserLogin(@PathVariable login: String): ResponseEntity<String> {
//		val user = userService.getUserByLogin(login)
//
//		if (user.isEmpty) {
//			return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
//		}
//		val phoneNumber: String = user.get().phone
//		return ResponseEntity.status(HttpStatus.OK).body(phoneNumber)
//	}

//	@GetMapping("/user/all")
//	fun getAllUsers(): ResponseEntity<List<User>> =
//		ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers())

//	@GetMapping("/user/email/{email}")
//	fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
//		val user = userService.getUserByEmail(email)
//		if (user.isEmpty) {
//			return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
//		}
//		return ResponseEntity.status(HttpStatus.OK).body(user.get())
//	}

	// ???/
	@GetMapping("/users/emailList")
	fun getAllUserEmails(): ResponseEntity<List<String>> {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserEmails())
	}

	// ??/?
	@GetMapping("/users/phoneList")
	fun getAllUserPhones(): ResponseEntity<List<String>> {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserPhones())
	}

	@DeleteMapping("/user/{login}/delete")
	fun deleteUserById(@PathVariable login: String): ResponseEntity<String> {
		userService.deleteUserByLogin(login)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}