package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Token
import pl.umk.mat.zesp01.pz2022.researcher.repository.TokenRepository
import java.util.*

@Service
class TokenService (@Autowired val tokenRepository: TokenRepository){
//=================================================ADD METHODS============================================================//
    fun addToken(token: Token): Token {
        token.id = IdGenerator().generateTokenId()
        return tokenRepository.insert(token)
    }

//=================================================DELETE METHODS============================================================//
    fun deleteToken(id: String) = tokenRepository.deleteById(id)

//=================================================GET METHODS============================================================//
    fun getAllTokens() : List<Token> = tokenRepository.findAll()

    fun getTokenById(id: String) : Optional<Token> = tokenRepository.findTokenById(id)

    fun getTokensByLogin(login: String) : List<Token> = tokenRepository.findTokensByLogin(login)

    fun getTokenByExpires(date: String) : List<Token> = tokenRepository.findTokensByExpires(date)

    fun getTokenByJwt(jwt: String) : Optional<Token> = tokenRepository.findTokenByJwt(jwt)

}