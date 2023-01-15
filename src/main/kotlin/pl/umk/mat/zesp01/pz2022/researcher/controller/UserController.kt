package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import org.mindrot.jbcrypt.BCrypt
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@RestController
class UserController(@Autowired val userService: UserService, @Autowired val userRepository: UserRepository) {

    /*** POST MAPPINGS ***/

    @PostMapping("/addUser")
    fun addUser(@RequestBody user: User): ResponseEntity<String> {

        if (userService.getUserByEmail(user.email).id != "") {
            return ResponseEntity.status(299).build()
        }
        if (userService.getUserByLogin(user.login).id != "") {
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

    @GetMapping("/getPhoneByUserLogin/{login}")
    fun getPhoneByUserLogin(@PathVariable login: String): ResponseEntity<String> {
        val user: User = userService.getUserByLogin(login)
        val phoneNumber: String = user.phone
        return ResponseEntity.status(HttpStatus.OK).body(phoneNumber)
    }

    @GetMapping("/getUsers")
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers())

    @GetMapping("/getUserById/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<User> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id))

    @GetMapping("/getUserByEmail/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(email))

    @GetMapping("/getUserByFirstName/{firstName}")
    fun getUserByFirstName(@PathVariable firstName: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUserByFirstName(firstName))

    @GetMapping("/getUserByLastName/{lastName}")
    fun getUserByLastName(@PathVariable lastName: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUserByLastName(lastName))

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/deleteUserById/{id}")
    fun deleteUserById(@PathVariable id: String): ResponseEntity<String> {
        userService.deleteUserById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}