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

	fun createRefreshToken(username: String): String {
		val payload = mapOf(Pair("username", username))
		return JWT
			.create()
			.withPayload(payload)
			.withExpiresAt(Date(System.currentTimeMillis() + REFRESH_EXPIRES_SEC * 1000))
			.sign(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
	}

	fun verifyRefreshToken(jwt: String): String? {
		val token = refreshTokenRepository.findRefreshTokenByJwt(jwt)

		if (token.isPresent) {
			return null
		}

		return try {
			val decoded = JWT
				.require(Algorithm.HMAC256(REFRESH_TOKEN_SECRET))
				.withClaimPresence("username")
				.build()
				.verify(jwt)

			val username = decoded.claims.getValue("username").toString()

			username.substring(1, username.length - 1)

		} catch (e: Exception) {
			null
		}
	}

	fun verifyAccessToken(jwt: String): String? {
		return try {
			val decoded = JWT
				.require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
				.withClaimPresence("username")
				.build()
				.verify(jwt)

			val usernameClaim = decoded.getClaim("username").toString()
			usernameClaim.substring(1, usernameClaim.length - 1)
		} catch (e: Exception) {
			null
		}
	}

//	fun verifyAccessToken(jwt: String, username: String): String? {
//		return try {
//			val decoded = JWT
//				.require(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
//				.withClaim("username", username)
//				.build()
//				.verify(jwt)
//
//			val usernameClaim = decoded.getClaim("username").toString()
//
//			usernameClaim.substring(1, username.length - 1)
//		} catch (e: Exception) {
//			null
//		}
//	}

}