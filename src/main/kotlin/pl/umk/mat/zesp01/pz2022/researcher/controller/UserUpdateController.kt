package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@RestController
class UserUpdateController(@Autowired val userRepository: UserRepository) {

    @PutMapping("/updateUser/{id}")
    fun updateUser(@PathVariable id: String, @RequestBody user: User): ResponseEntity<User> {
        val oldUser = userRepository.findById(id).orElse(null)
        user.id = oldUser.id
        if (user.login.isEmpty()) user.login = oldUser.login
        if (user.password.isEmpty()) user.password = oldUser.password else user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())
        if (user.firstName.isEmpty()) user.firstName = oldUser.firstName
        if (user.lastName.isEmpty()) user.lastName = oldUser.lastName
        if (user.email.isEmpty()) user.email = oldUser.email
        if (user.phone.isEmpty()) user.phone = oldUser.phone
        if (user.birthDate.isEmpty()) user.birthDate = oldUser.birthDate
        if (user.gender.isEmpty()) user.gender = oldUser.gender
        if (user.avatarImage.isEmpty()) user.avatarImage = oldUser.avatarImage

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user))
    }
}