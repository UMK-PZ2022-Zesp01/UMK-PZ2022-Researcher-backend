package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import java.util.*

@Repository
interface UserRepository : MongoRepository<User, String> {
	fun findUserByLogin(login: String): Optional<User>
	fun deleteUserByLogin(login: String): Optional<User>
}