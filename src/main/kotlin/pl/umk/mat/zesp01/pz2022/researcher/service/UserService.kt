package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@Service
class UserService(@Autowired val userRepository: UserRepository) {
    /* ___________________________________ADD METHODS___________________________________*/
    fun addUser(user: User): User = userRepository.insert(user)

    /* ___________________________________DELETE METHODS___________________________________*/
    fun deleteUserById(id: String) = userRepository.deleteById(id)

    /* ___________________________________GET METHODS___________________________________*/
    fun getAllUsers(): List<User> = userRepository.findAll()
    fun getUserById(id: String): User = userRepository.findById(id)
        .orElseThrow { throw RuntimeException("Cannot find User by Id") }

    fun getUserByFirstName(firstName: String): List<User> = userRepository.findUserByFirstName(firstName)
        .orElseThrow { throw RuntimeException("Cannot find User by First name") }

    fun getUserByLastName(lastName: String): List<User> = userRepository.findUserByLastName(lastName)
        .orElseThrow { throw RuntimeException("Cannot find User by Last name") }

    fun getUserByEmail(email: String): User =
        userRepository.findUserByEmail(email).orElseThrow { throw RuntimeException("Cannot find User by Email") }

}