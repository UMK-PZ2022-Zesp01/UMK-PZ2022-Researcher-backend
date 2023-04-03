package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
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
class RefreshTokenService(
	@Autowired val refreshTokenRepository: RefreshTokenRepository
) {

	fun addToken(jwt: String): RefreshToken {
		val expiryDate = JWT.decode(jwt).expiresAt.toString()
		val refreshToken = RefreshToken(jwt = jwt, expires = expiryDate)
		return refreshTokenRepository.insert(refreshToken)
	}

	fun getTokenByJwt(jwt: String): Optional<RefreshToken> =
		refreshTokenRepository.findRefreshTokenByJwt(jwt)

    fun createAccessToken(username: String): String {
        val payload = mapOf(Pair("username", username))
        return JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
    }

    fun createRefreshToken(username: String): String? {
        try {
            val expires = Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000)
            val payload = mapOf(Pair("username", username))
            val refreshToken = JWT
                .create()
                .withPayload(payload)
                .withExpiresAt(expires)
                .sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
            val refreshTokenDB = RefreshToken(
                username = username,
                expires = expires.toString(),
                jwt = BCrypt.hashpw(refreshToken, BCrypt.gensalt()),
            )

            refreshTokenRepository.insert(refreshTokenDB)
            return refreshToken
        } catch (e: Exception) {
            return null
        }
    }

    fun getMatchingToken(plainJwt: String): RefreshToken? {
        try {
            val decoded = JWT
                .require(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
                .withClaimPresence("username")
                .build()
                .verify(plainJwt)
            val usernameClaim = decoded.claims.getValue("username").toString()
            val username = usernameClaim.substring(1, usernameClaim.length - 1)

            val userTokens = refreshTokenRepository.findRefreshTokensByUsername(username).get()
            if (userTokens.isEmpty()) throw Exception()

            val matchingTokens = userTokens.filter { token -> BCrypt.checkpw(plainJwt, token.jwt) }
            if (matchingTokens.isEmpty()) throw Exception()

            if (matchingTokens[0].username != username) throw Exception()

            return matchingTokens[0]
        } catch (e: Exception) {
            return null
        }
    }

    fun verifyRefreshToken(plainJwt: String): RefreshToken? {
        return try {
            getMatchingToken(plainJwt)
        } catch (e: Exception) {
            null
        }
    }

    fun verifyAccessToken(plainJwt: String): String? {
        return try {
            val decoded = JWT
                .require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
                .withClaimPresence("username")
                .build()
                .verify(plainJwt)

            val usernameClaim = decoded.claims.getValue("username").toString()
            usernameClaim.substring(1, usernameClaim.length - 1)
        } catch (e: Exception) {
            null
        }
    }

    fun removeRefreshToken(plainJwt: String): Boolean {
        return try {
            val token = getMatchingToken(plainJwt) ?: throw Exception()

            refreshTokenRepository.deleteRefreshTokensByJwt(token.jwt)
            true
        } catch (e: Exception) {
            false
        }
    }

//	fun addToken(jwt: String): RefreshToken {
//		val expiryDate = JWT.decode(jwt).expiresAt.toString()
//		val refreshToken = RefreshToken(jwt = jwt, expires = expiryDate)
//		return refreshTokenRepository.insert(refreshToken)
//	}

}