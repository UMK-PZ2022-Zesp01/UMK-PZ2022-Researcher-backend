package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.RefreshToken
import pl.umk.mat.zesp01.pz2022.researcher.repository.RefreshTokenRepository
import java.util.*


lateinit var ACCESS_TOKEN_SECRET: String
lateinit var REFRESH_TOKEN_SECRET: String
const val ACCESS_EXPIRES_SEC: Long = 900
const val REFRESH_EXPIRES_SEC: Long = 86400

@Service
class RefreshTokenService(@Autowired val refreshTokenRepository: RefreshTokenRepository) {

    /*** ADD METHODS ***/

    fun createRefreshToken(username:String):String{
        val payload = mapOf(Pair("username", username))

        val refreshToken = JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))

        val refreshTokenDB = RefreshToken()

        refreshTokenDB.id=IdGenerator().generateTokenId()
        refreshTokenDB.login=username
        refreshTokenDB.expires=Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000).toString()
        refreshTokenDB.jwt=refreshToken

        refreshTokenRepository.insert(refreshTokenDB)

        return refreshToken
    }

    fun createAccessToken(username:String):String{
        val payload = mapOf(Pair("username",username))

        return JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
    }

    fun verifyRefreshToken(jwt: String, username: String):Boolean{
        try {
            JWT
                .require(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
                .withClaim("username",username)
                .build()
                .verify(jwt)

            return true
        }catch (e:Exception){
            return false
        }
    }

    fun verifyAccessToken(jwt:String,username: String):Boolean{
        try {
            JWT
                .require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaim("username",username)
                .build()
                .verify(jwt)

            return true
        }catch (e:Exception){
            return false
        }
    }


    fun addToken(refreshToken: RefreshToken): RefreshToken {
        refreshToken.id = IdGenerator().generateTokenId()
        return refreshTokenRepository.insert(refreshToken)
    }

    /*** DELETE METHODS ***/

    fun deleteToken(id: String) = refreshTokenRepository.deleteById(id)

    /*** ADD METHODS ***/

    fun getAllTokens(): List<RefreshToken> = refreshTokenRepository.findAll()

    fun getTokenById(id: String): Optional<RefreshToken> = refreshTokenRepository.findTokenById(id)

    fun getTokensByLogin(login: String): Optional<List<RefreshToken>> = refreshTokenRepository.findTokensByLogin(login)

    fun getTokenByExpires(date: String): Optional<List<RefreshToken>> = refreshTokenRepository.findTokensByExpires(date)

    fun getTokenByJwt(jwt: String): Optional<RefreshToken> = refreshTokenRepository.findTokenByJwt(jwt)

    /*** DELETE METHODS***/
//    fun deleteExpiredTokens(){
//    }

}