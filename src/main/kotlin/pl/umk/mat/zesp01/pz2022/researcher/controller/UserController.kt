package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import org.mindrot.jbcrypt.BCrypt
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.UserRegisterData
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@RestController
class UserController(
    @Autowired val userService: UserService,
    @Autowired val userRepository: UserRepository,
    @Autowired val refreshTokenService: RefreshTokenService,
    @Autowired val verificationTokenService: VerificationTokenService,
    @Autowired val eventPublisher: ApplicationEventPublisher
) {
    val gson = Gson()

    /*** POST MAPPINGS ***/

    @PostMapping("/user/register")
    fun addUser(@RequestBody uRD: UserRegisterData): ResponseEntity<String> {

        if (userService.getUserByEmail(uRD.email).isPresent) {
            return ResponseEntity.status(299).build()
        }
        if (userService.getUserByLogin(uRD.login).isPresent) {
            return ResponseEntity.status(298).build()
        }

        val newUser = uRD.toUser()

        //blok poniżej chyba należy przenieść do UserService
        newUser.password = BCrypt.hashpw(newUser.password, BCrypt.gensalt())
        newUser.id = IdGenerator().generateUserId(userService.getAllUserIds())
        userService.addUser(newUser)

        try {
            val appUrl=ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            eventPublisher.publishEvent(OnRegistrationCompleteEvent(appUrl,newUser))
        }catch (e:Exception){
            println(e)
        }




        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/user/confirm")
    fun confirmAccount(
        @RequestParam("token") token:String
    ){
        val verificationToken = verificationTokenService.getTokenByJwt(token)
        if (verificationToken.isEmpty){
            TODO("error: Nieprawidłowy token")
        }

        val user = userService.getUserByLogin(verificationToken.get().login)
        if(user.isEmpty){
            TODO("error: nawet nie wiem w jaki sposób ma ta sytuacja zaistnieć")
            println()
        }

        verificationTokenService.verifyVerificationToken(
            verificationToken.get().jwt,
            user.get().login)

        user.get().isConfirmed=true

        userService.userRepository.save(user.get())
    }


    /*** PUT MAPPINGS ***/

    @PutMapping("/user/{id}/update")
    fun updateUser(@PathVariable id: String, @RequestBody user: User): ResponseEntity<User> {
        val oldUser = userRepository.findById(id).orElse(null)
        user.id = oldUser.id
        if (user.login.isEmpty()) user.login = oldUser.login
        if (user.password.isEmpty()) user.password = oldUser.password
        else user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
        if (user.firstName.isEmpty()) user.firstName = oldUser.firstName
        if (user.lastName.isEmpty()) user.lastName = oldUser.lastName
        if (user.email.isEmpty()) user.email = oldUser.email
        if (user.phone.isEmpty()) user.phone = oldUser.phone
        if (user.birthDate.isEmpty()) user.birthDate = oldUser.birthDate
        if (user.gender.isEmpty()) user.gender = oldUser.gender
        if (user.avatarImage.isEmpty()) user.avatarImage = oldUser.avatarImage

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user))
    }

    /*** GET MAPPINGS ***/

    @GetMapping("/user/current")
    fun getUserProfile(
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
        jwt ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        try {
            val decoded = JWT.require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaimPresence("username")
                .build()
                .verify(jwt[0])

            //get the username claimed in the access token
            var username = decoded
                .claims
                .getValue("username")
                .toString()
            username = username.substring(1,username.length-1)

            //If claimed user does not exist in db there is something wrong with the token
            val user = userService.getUserByLogin(username)
            if (user.isEmpty) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

            //
            val data= userService
                .getUserByLogin(username)
                .get()
                .toUserProfileDTO()


            return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(data))
        } catch (e: java.lang.Exception) {
            println(e)
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
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

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/user/{id}/delete")
    fun deleteUserById(@PathVariable id: String): ResponseEntity<String> {
        userService.deleteUserById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}