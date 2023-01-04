package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*

@Service
class UserService(@Autowired val userRepository: UserRepository) {

    fun addUser(user: User): User = userRepository.insert(user)
    fun getAll(): List<User> = userRepository.findAll()
    fun deleteById(id: String) = userRepository.deleteById(id)
    fun getUserByName(name: String): User = userRepository.findUserByFirstName(name)
            .orElseThrow { throw RuntimeException("Cannot find User by First name") }

    fun getUserById(id: String): User = userRepository.findById(id)
            .orElseThrow { throw RuntimeException("Cannot find User by Id") }
}