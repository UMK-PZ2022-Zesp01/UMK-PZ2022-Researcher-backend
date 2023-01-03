package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.UUID.randomUUID
import org.mindrot.jbcrypt.BCrypt

@RestController
class UserController(@Autowired val userService: UserService) {

    @PostMapping("/addUser")
    fun addUser(@RequestBody user: User): ResponseEntity<String> {
        //tutaj trzeba zrobić sprawdzanie zeby loginy sie nie powtarzaly
        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
        user.id = randomUUID().toString()
        userService.addUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/getAll")
    fun getAll(): ResponseEntity<List<User>> = ResponseEntity.status(HttpStatus.OK).body(userService.getAll())

    @GetMapping("/getUserByName/{firstName}")
    fun getUserByName(@PathVariable firstName:String) : ResponseEntity<User>
            = ResponseEntity.status(HttpStatus.OK).body(userService.getUserByName(firstName))
    @GetMapping("/getUserById/{id}")
    fun getUserById(@PathVariable id:String) : ResponseEntity<User>
            = ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id))
    @DeleteMapping("/deleteById/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<String> {
        userService.deleteById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}