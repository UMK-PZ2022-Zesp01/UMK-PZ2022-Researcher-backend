package pl.umk.mat.zesp01.pz2022.researcher.common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.HttpHeaders
import java.util.*

fun verifyJWT(httpHeaders: HttpHeaders, key: String, username: String): Boolean {
    val authHeader = httpHeaders["authorization"] ?: return false

    return try {
        val algorithm = Algorithm.HMAC256(key)
        val decoded = JWT
            .require(algorithm)
            .withClaim("username", username)
            .build()
            .verify(authHeader[0])
        true
    } catch (error: JWTVerificationException) {
        println("Authorization failed.")
        false
    }
}
