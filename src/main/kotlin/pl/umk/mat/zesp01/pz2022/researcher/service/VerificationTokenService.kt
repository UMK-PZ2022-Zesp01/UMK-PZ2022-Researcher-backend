package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import java.util.*

lateinit var VERIFICATION_TOKEN_SECRET: String
const val VERIFICATION_EXPIRES_SEC = 86400

@Service
class VerificationTokenService(
	@Autowired val verificationTokenRepository: VerificationTokenRepository
) {

	fun createToken(username: String): String {
		val payload = mapOf(Pair("username", username))

		val verificationToken = JWT
			.create()
			.withPayload(payload)
			.withExpiresAt(Date(System.currentTimeMillis() + VERIFICATION_EXPIRES_SEC * 1000))
			.sign(Algorithm.HMAC256(VERIFICATION_TOKEN_SECRET))

		val verificationTokenDB = VerificationToken(
			login = username,
			expires = Date(System.currentTimeMillis() + VERIFICATION_EXPIRES_SEC * 1000).toString(),
			jwt = verificationToken
		)

		verificationTokenRepository.insert(verificationTokenDB)

		return verificationToken
	}

	fun verifyToken(jwt: String): String? {
		return try {
			val decoded = JWT
				.require(Algorithm.HMAC256(VERIFICATION_TOKEN_SECRET))
				.withClaimPresence("username")
				.build()
				.verify(jwt)

			val usernameClaim = decoded.getClaim("username").toString()
			usernameClaim.substring(1, usernameClaim.length - 1)
		} catch (e: Exception) {
			null
		}
	}

//	fun addToken(token: VerificationToken): VerificationToken {
//		return verificationTokenRepository.insert(token)
//	}

	fun getTokenByJwt(jwt: String): Optional<VerificationToken> =
		verificationTokenRepository.findVerificationTokenByJwt(jwt)

	fun deleteUserTokens(user: User) {
		val tokens = verificationTokenRepository.findVerificationTokensByLogin(user.login).get()
		tokens.forEach { verificationToken -> deleteTokenByJwt(verificationToken.jwt) }
	}

	fun deleteTokenByJwt(jwt: String) =
		verificationTokenRepository.deleteVerificationTokenByJwt(jwt)

}


