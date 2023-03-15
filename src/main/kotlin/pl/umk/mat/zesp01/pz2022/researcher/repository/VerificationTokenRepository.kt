package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import java.util.*

@Repository
interface VerificationTokenRepository : MongoRepository<VerificationToken, String> {
    @Query("{'id':?0}")
    fun findTokenById(id: String): Optional<VerificationToken>

    @Query("{'login':?0}")
    fun findTokensByLogin(userId: String): Optional<List<VerificationToken>>

    @Query("{'expires':?0}")
    fun findTokensByExpires(userId: String): Optional<List<VerificationToken>>

    @Query("{'jwt':?0}")
    fun findTokenByJwt(userId: String): Optional<VerificationToken>
}