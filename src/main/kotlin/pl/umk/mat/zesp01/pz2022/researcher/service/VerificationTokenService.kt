package pl.umk.mat.zesp01.pz2022.researcher.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties.User
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import java.util.*

val VERIFICATION_TOKEN_SECRET: String = System.getenv("VERIFICATION_TOKEN_SECRET")
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

    fun verifyVerificationToken(jwt: String, username: String):Boolean{
        try {
            JWT
                .require(Algorithm.HMAC256(VERIFICATION_TOKEN_SECRET))
                .withClaim("username",username)
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