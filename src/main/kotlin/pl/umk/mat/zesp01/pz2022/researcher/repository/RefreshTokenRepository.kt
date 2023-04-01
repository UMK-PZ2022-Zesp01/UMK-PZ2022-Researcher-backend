package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.RefreshToken
import java.util.*

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
	@Query("{'id':?0}")
	fun findTokenById(id: String): Optional<RefreshToken>

	@Query("{'login':?0}")
	fun findTokensByLogin(userId: String): Optional<List<RefreshToken>>

	@Query("{'expires':?0}")
	fun findTokensByExpires(userId: String): Optional<List<RefreshToken>>

	@Query("{'jwt':?0}")
	fun findTokenByJwt(userId: String): Optional<RefreshToken>
}