package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import org.mindrot.jbcrypt.BCrypt
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpHeaders
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@RestController
class UserController(
    @Autowired val userService: UserService,
    @Autowired val userRepository: UserRepository
) {

    val gson = Gson()

    /*** POST MAPPINGS ***/

    @PostMapping("/addUser")
    fun addUser(@RequestBody user: User): ResponseEntity<String> {

        if (userService.getUserByEmail(user.email).isEmpty) {
            return ResponseEntity.status(299).build()
        }
        if (userService.getUserByLogin(user.login).isEmpty) {
            return ResponseEntity.status(298).build()
        }
        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
        user.id = IdGenerator().generateUserId()
        userService.addUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /*** PUT MAPPINGS ***/

    @PutMapping("/updateUser/{id}")
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

    @GetMapping("/getUserProfile")
    fun getUserProfile(
        @RequestHeader httpHeaders: HttpHeaders,
        @RequestBody profileUsername: String
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
        jwt ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        try {
            val decoded = JWT.require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaimPresence("username")
                .build()
                .verify(jwt[0])

            //get the username claimed in the access token
            val username = decoded
                .claims
                .getValue("username")
                .toString()

            //If claimed user does not exist in db there is something wrong with the token
            val user = userService.getUserByLogin(username)
            if (user.isEmpty) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

            //
            val profile = userService
                .getUserByLogin(profileUsername)
                .get()
                .toUserProfileDTO()


            return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(profile))
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

    @GetMapping("/getUsers")
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers())

    @GetMapping("/getUserById/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<User> {
        val user = userService.getUserById(id)
        if (user.isEmpty) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
        return ResponseEntity.status(HttpStatus.OK).body(user.get())
    }

    @GetMapping("/getUserByEmail/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> {
        val user = userService.getUserByEmail(email)
        if (user.isEmpty) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
        return ResponseEntity.status(HttpStatus.OK).body(user.get())
    }

    @GetMapping("/getUserByFirstName/{firstName}")
    fun getUserByFirstName(@PathVariable firstName: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByFirstName(firstName))

    @GetMapping("/getUserByLastName/{lastName}")
    fun getUserByLastName(@PathVariable lastName: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByLastName(lastName))

    @GetMapping("/getUsersByGender/{gender}")
    fun findUsersByGender(@PathVariable gender: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.findUsersByGender(gender))

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/deleteUserById/{id}")
    fun deleteUserById(@PathVariable id: String): ResponseEntity<String> {
        userService.deleteUserById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}