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

    fun getUserById(id: String): Optional<User> = userRepository.findById(id)

    fun getUserByEmail(email: String): Optional<User> = userRepository.findUserByEmail(email)
    //        .orElseThrow { throw RuntimeException("Cannot find User by Email") }

    fun getUserByLogin(login: String): Optional<User> = userRepository.findUserByLogin(login)
    //        .orElseThrow { throw RuntimeException("Cannot find User by Login") }

    fun getUsersByFirstName(firstName: String): List<User> = userRepository.findUserByFirstName(firstName)
        .orElseThrow { throw RuntimeException("Cannot find User by First name") }

    fun getUsersByLastName(lastName: String): List<User> = userRepository.findUserByLastName(lastName)
        .orElseThrow { throw RuntimeException("Cannot find User by Last name") }
}