package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import java.util.*

@Repository
interface UserRepository : MongoRepository<User, String> {
    @Query("{'firstName':?0}")
    fun findUserByFirstName(firstName: String): Optional<User>

    @Query("{'lastName':?0}")
    fun findUserByLastName(lastName: String): Optional<User>
}