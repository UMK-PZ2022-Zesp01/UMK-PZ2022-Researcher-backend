package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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

    fun createRefreshToken(username: String): String {
        val payload = mapOf(Pair("username", username))

        return JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
    }

    fun createAccessToken(username: String): String {
        val payload = mapOf(Pair("username", username))

        return JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
    }

    fun verifyRefreshToken(jwt: String): String? {
        val token = refreshTokenRepository.findTokenByJwt(jwt)

        if (token.isPresent){
            return null
        }

        try {
             val decoded = JWT
                 .require(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
                 .withClaimPresence("username")
                 .build()
                 .verify(jwt)

            val username = decoded.claims.getValue("username").toString()

            return username.substring(1, username.length-1)

        } catch (e: Exception) {
            return null
        }
    }

    fun verifyAccessToken(jwt: String): String? {
        try {
            val decoded = JWT
                .require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaimPresence("username")
                .build()
                .verify(jwt)

            val usernameClaim = decoded.getClaim("username").toString()

            return usernameClaim.substring(1, usernameClaim.length-1)

        } catch (e: Exception) {
            return null
        }
    }

    fun verifyAccessToken(jwt: String, username: String): String? {
        try {
            val decoded = JWT
                .require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaim("username", username)
                .build()
                .verify(jwt)

            val usernameClaim = decoded.getClaim("username").toString()

            return usernameClaim.substring(1, username.length-1)
        } catch (e: Exception) {
            return null
        }
    }

//    fun addToken(refreshToken: RefreshToken): RefreshToken {
//        refreshToken.id = IdGenerator().generateTokenId()
//        return refreshTokenRepository.insert(refreshToken)
//    }

    fun addToken(jwt: String): RefreshToken {
        val expiryDate = JWT.decode(jwt).expiresAt.toString()

        val refreshToken = RefreshToken( jwt = jwt, expires = expiryDate)

        return refreshTokenRepository.insert(refreshToken)
    }

    /*** DELETE METHODS ***/

    fun deleteTokenById(id: String) =
        refreshTokenRepository.deleteById(id)

    /*** ADD METHODS ***/

    fun getAllTokens(): List<RefreshToken> =
        refreshTokenRepository.findAll()

    fun getTokenById(id: String): Optional<RefreshToken> =
        refreshTokenRepository.findTokenById(id)

    fun getTokensByLogin(login: String): Optional<List<RefreshToken>> =
        refreshTokenRepository.findTokensByLogin(login)

    fun getTokenByExpires(date: String): Optional<List<RefreshToken>> =
        refreshTokenRepository.findTokensByExpires(date)

    fun getTokenByJwt(jwt: String): Optional<RefreshToken> =
        refreshTokenRepository.findTokenByJwt(jwt)

    /*** DELETE METHODS***/

//    fun deleteExpiredTokens(){
//    }

}