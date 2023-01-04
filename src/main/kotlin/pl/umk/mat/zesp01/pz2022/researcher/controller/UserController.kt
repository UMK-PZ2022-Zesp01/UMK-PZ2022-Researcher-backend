package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import org.mindrot.jbcrypt.BCrypt
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator

@RestController
class UserController(@Autowired val userService: UserService) {
    /* ___________________________________POST MAPPINGS___________________________________*/
    @PostMapping("/addUser")
    fun addUser(@RequestBody user: User): ResponseEntity<String> {
        //tutaj trzeba zrobić sprawdzanie zeby loginy sie nie powtarzaly
        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
        user.id = IdGenerator().generateUserId()
        userService.addUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /* ___________________________________GET MAPPINGS___________________________________*/
    @GetMapping("/getUsers")
    fun getAll(): ResponseEntity<List<User>> =
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

    @GetMapping("/getUserByLastName/{firstName}")
    fun getUserByLastName(@PathVariable lastName: String): ResponseEntity<List<User>> =
        ResponseEntity.status(HttpStatus.OK).body(userService.getUserByLastName(lastName))

    /* ___________________________________DELETE MAPPINGS___________________________________*/
    @DeleteMapping("/deleteUserById/{id}")
    fun deleteUserById(@PathVariable id: String): ResponseEntity<String> {
        userService.deleteUserById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}