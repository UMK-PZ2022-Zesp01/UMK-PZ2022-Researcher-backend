package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Token
import java.util.*

@Repository
interface TokenRepository :MongoRepository <Token, String> {
    @Query("{'id':?0}")
    fun findTokenById(id: String): Optional<Token>

    @Query("{'login':?0}")
    fun findTokensByLogin(userId: String):List<Token>

    @Query("{'expires':?0}")
    fun findTokensByExpires(userId: String):List<Token>

    @Query("{'jwt':?0}")
    fun findTokenByJwt(userId: String):Optional<Token>
}