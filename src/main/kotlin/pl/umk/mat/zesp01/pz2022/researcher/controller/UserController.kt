package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import org.mindrot.jbcrypt.BCrypt
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import pl.umk.mat.zesp01.pz2022.researcher.model.UserRegisterRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@RestController
class UserController(
	@Autowired val userService: UserService,
	@Autowired val userRepository: UserRepository,
	@Autowired val verificationTokenService: VerificationTokenService,
	@Autowired val refreshTokenService: RefreshTokenService,
	@Autowired val eventPublisher: ApplicationEventPublisher,
) {
	val gson = Gson()

	/*** POST MAPPINGS ***/

	@PostMapping("/user/register")
	fun addUser(@RequestBody uRR: UserRegisterRequest): ResponseEntity<String> {

		if (userService.getUserByEmail(uRR.email).isPresent) {
			return ResponseEntity.status(299).build()
		}
		if (userService.getUserByLogin(uRR.login).isPresent) {
			return ResponseEntity.status(298).build()
		}

		val newUser = uRR.toUser()
		userService.addUser(newUser)

		return ResponseEntity.status(HttpStatus.CREATED).build()
	}

	// 'return' lifted out of 'try', check whether it causes any errors
	@GetMapping("/user/sendVerificationMail")
	fun sendVerificationEmail(
		@RequestParam("username") username: String
	): ResponseEntity<String> {
		return try {
			val user = userService.getUserByLogin(username).orElseThrow()
			if (user.isConfirmed) throw (Exception())

			verificationTokenService.deleteUserTokens(user)

			eventPublisher.publishEvent(OnRegistrationCompleteEvent(user))
			ResponseEntity.status(HttpStatus.CREATED).body(gson.toJson(user.email))
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}
	}

	@GetMapping("/user/confirm")
	fun confirmAccount(@RequestParam("token") token: String): ResponseEntity<String> {
		try {
			val verificationToken = verificationTokenService
				.getTokenByJwt(token)
				.orElseThrow()
//                TODO("error: Nieprawidłowy token")

			val user = userService
				.getUserByLogin(verificationToken.login)
				.orElseThrow()
//                TODO("error: nawet nie wiem w jaki sposób ma ta sytuacja zaistnieć")

			if (user.isConfirmed) return ResponseEntity.status(HttpStatus.NO_CONTENT).build()

			verificationTokenService.verifyVerificationToken(
				verificationToken.jwt,
				user
			)

			val activeUser = user.copy(isConfirmed = true)

			userService.userRepository.save(activeUser)

			verificationTokenService.deleteUserTokens(user)
			return ResponseEntity.status(HttpStatus.CREATED).build()
		} catch (_: Exception) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}
	}


	/*** PUT MAPPINGS ***/

	@PutMapping("/user/update")
	fun updateUser(@RequestBody user: UserUpdateRequest): ResponseEntity<User> {
		val oldUser = userService.getUserByLogin(user.login).orElse(User())

		val updatedUser = User(
			login =  if (user.login.isEmpty())(oldUser.login) else (user.login),
			password =  if (user.password.isEmpty())(oldUser.password) else BCrypt.hashpw(user.password, BCrypt.gensalt()),
			firstName =  if (user.firstName.isEmpty())(oldUser.firstName) else (user.firstName),
			lastName =  if (user.lastName.isEmpty())(oldUser.lastName) else (user.lastName),
			email =  if (user.email.isEmpty())(oldUser.email) else (user.email),
			phone =  if(user.phone.isEmpty())(oldUser.phone) else (user.phone),
			birthDate =  if(user.birthDate.isEmpty())(oldUser.birthDate) else (user.birthDate),
			gender =  if(user.gender.isEmpty())(oldUser.gender) else (user.gender),
			avatarImage =  if(user.avatarImage.isEmpty())(oldUser.avatarImage) else (user.avatarImage),
		)
		return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(updatedUser))
	}

	/*** GET MAPPINGS ***/

	@GetMapping("/user/current")
	fun getUserProfile(@RequestHeader httpHeaders: HttpHeaders): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
		jwt ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		try {
			//get the username claimed in the access token
			val username = refreshTokenService.verifyAccessToken(jwt[0])
			if (username.isNullOrEmpty())return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

			//If claimed user does not exist in db there is something wrong with the token
			val user = userService.getUserByLogin(username)
			if (user.isEmpty) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

			val data = userService
				.getUserByLogin(username)
				.get()
				.toUserResponse()

			return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(data))
		} catch (e: java.lang.Exception) {
			println(e)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}





	@GetMapping("/getPhoneByUserLogin/{login}")
	fun getPhoneByUserLogin(@PathVariable login: String): ResponseEntity<String> {
		val user = userService.getUserByLogin(login)

		if (user.isEmpty) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}
		val phoneNumber: String = user.get().phone
		return ResponseEntity.status(HttpStatus.OK).body(phoneNumber)
	}

	@GetMapping("/users")
	fun getAllUsers(): ResponseEntity<List<User>> =
		ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers())

	@GetMapping("/user/{id}")
	fun getUserById(@PathVariable id: String): ResponseEntity<User> {
		val user = userService.getUserById(id)
		if (user.isEmpty) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}
		return ResponseEntity.status(HttpStatus.OK).body(user.get())
	}

	@GetMapping("/user/email/{email}")
	fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
		val user = userService.getUserByEmail(email)
		if (user.isEmpty) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		}
		return ResponseEntity.status(HttpStatus.OK).body(user.get())
	}

	@GetMapping("/users/firstName/{firstName}")
	fun getUserByFirstName(@PathVariable firstName: String): ResponseEntity<List<User>> =
		ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByFirstName(firstName))

	@GetMapping("/users/lastName/{lastName}")
	fun getUserByLastName(@PathVariable lastName: String): ResponseEntity<List<User>> =
		ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByLastName(lastName))

	@GetMapping("/users/gender/{gender}")
	fun findUsersByGender(@PathVariable gender: String): ResponseEntity<List<User>> =
		ResponseEntity.status(HttpStatus.OK).body(userService.findUsersByGender(gender))

	@GetMapping("/users/idList")
	fun getAllUserIds(): ResponseEntity<List<String>> {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserIds())
	}

	@GetMapping("/users/emailList")
	fun getAllUserEmails():ResponseEntity<List<String>>{
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserEmails())
	}

	@GetMapping("/users/phoneList")
	fun getAllUserPhones():ResponseEntity<List<String>>{
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUserPhones())
	}

	/*** DELETE MAPPINGS ***/

	@DeleteMapping("/user/{id}/delete")
	fun deleteUserById(@PathVariable id: String): ResponseEntity<String> {
		userService.deleteUserById(id)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}