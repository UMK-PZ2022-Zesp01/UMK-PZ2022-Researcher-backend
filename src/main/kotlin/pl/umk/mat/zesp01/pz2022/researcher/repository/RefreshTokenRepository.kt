package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.RefreshToken
import java.util.*

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
	fun findRefreshTokenByJwt(jwt: String): Optional<RefreshToken>
	fun findRefreshTokensByUsername(username: String): Optional<List<RefreshToken>>
	fun deleteRefreshTokensByJwt(jwt: String)
}