package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import java.util.*


val VERIFICATION_TOKEN_SECRET: String = verificationTokenSecretInitialize()
const val VERIFICATION_EXPIRES_SEC = 86400

@Service
class VerificationTokenService(@Autowired val verificationTokenRepository: VerificationTokenRepository) {

    /*** ADD METHODS ***/

    fun createToken(username:String):String{
        val payload = mapOf(Pair("username", username))

        val verificationToken = JWT
            .create()
            .withPayload(payload)
            .withExpiresAt(Date(System.currentTimeMillis() + VERIFICATION_EXPIRES_SEC * 1000))
            .sign(Algorithm.HMAC256(VERIFICATION_TOKEN_SECRET))

        val verificationTokenDB = VerificationToken()

        verificationTokenDB.id=IdGenerator().generateTokenId()
        verificationTokenDB.login=username
        verificationTokenDB.expires=Date(System.currentTimeMillis() + VERIFICATION_EXPIRES_SEC * 1000).toString()
        verificationTokenDB.jwt=verificationToken

        verificationTokenRepository.insert(verificationTokenDB)

        return verificationToken
    }

    fun verifyVerificationToken(jwt: String, user:User):Boolean{
        try {
            JWT
                .require(Algorithm.HMAC256(VERIFICATION_TOKEN_SECRET))
                .withClaim("username",user.login)
                .build()
                .verify(jwt)

            return true
        }catch (e:Exception){
            return false
        }
    }

    fun addToken(token: VerificationToken): VerificationToken {
        token.id = IdGenerator().generateTokenId()
        return verificationTokenRepository.insert(token)
    }

    /*** DELETE METHODS ***/

    fun deleteUserTokens(user:User){
        val tokens = getTokensByLogin(user.login).orElse(null)

        tokens?:return

        tokens.forEach{ verificationToken -> deleteToken(verificationToken.id) }
    }

    fun deleteToken(id: String) = verificationTokenRepository.deleteById(id)

    //    fun deleteExpiredTokens(){
//    }

    /*** ADD METHODS ***/

    fun getAllTokens(): List<VerificationToken> = verificationTokenRepository.findAll()

    fun getTokenById(id: String): Optional<VerificationToken> = verificationTokenRepository.findTokenById(id)

    fun getTokensByLogin(login: String): Optional<List<VerificationToken>> = verificationTokenRepository.findTokensByLogin(login)

    fun getTokenByExpires(date: String): Optional<List<VerificationToken>> = verificationTokenRepository.findTokensByExpires(date)

    fun getTokenByJwt(jwt: String): Optional<VerificationToken> = verificationTokenRepository.findTokenByJwt(jwt)

}

/*** Token initialize method ***/

private fun verificationTokenSecretInitialize(): String{
    if (System.getenv("VERIFICATION_TOKEN_SECRET")!=null)
        return System.getenv("VERIFICATION_TOKEN_SECRET")

    // if environment variable is not specified return Token_SECRET_for_Tests
    return "beed319b8d87854c699a7ee5b7682dda42f5e85bf373d86774eb3ece227099eb87ad77e72e7a1facc2f2ebad4bef340d188fa01c2cfefd7380857e9aeff268b2"
}