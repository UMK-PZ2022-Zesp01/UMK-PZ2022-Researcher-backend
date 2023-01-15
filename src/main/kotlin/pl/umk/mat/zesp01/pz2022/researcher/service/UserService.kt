package pl.umk.mat.zesp01.pz2022.researcher.service

import org.bson.json.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*

@Service
class UserService(@Autowired val userRepository: UserRepository) {

    /*** ADD METHODS ***/

    fun addUser(user: User): User = userRepository.insert(user)


    /*** DELETE METHODS ***/

    fun deleteUserById(id: String) = userRepository.deleteById(id)


    /*** GET METHODS ***/

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: String): User = userRepository.findById(id)
        .orElseThrow { throw RuntimeException("Cannot find User by Id") }

    fun getUserByEmail(email: String): User = userRepository.findUserByEmail(email)
        .orElse(User())
    // .orElseThrow { throw RuntimeException("Cannot find User by Email") }

    fun getUserByLogin(login: String): User = userRepository.findUserByLogin(login)
        .orElse(User())
    // .orElseThrow { throw RuntimeException("Cannot find User by Login") }

    fun getUserByFirstName(firstName: String): List<User> = userRepository.findUserByFirstName(firstName)
        .orElseThrow { throw RuntimeException("Cannot find User by First name") }

    fun getUserByLastName(lastName: String): List<User> = userRepository.findUserByLastName(lastName)
        .orElseThrow { throw RuntimeException("Cannot find User by Last name") }
}