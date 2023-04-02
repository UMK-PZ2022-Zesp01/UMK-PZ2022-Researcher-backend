package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import java.util.*

@Repository
interface VerificationTokenRepository : MongoRepository<VerificationToken, String> {
	fun findVerificationTokensByLogin(login: String): Optional<List<VerificationToken>>
	fun findVerificationTokenByJwt(jwt: String): Optional<VerificationToken>
	fun deleteVerificationTokenByJwt(jwt: String)

}