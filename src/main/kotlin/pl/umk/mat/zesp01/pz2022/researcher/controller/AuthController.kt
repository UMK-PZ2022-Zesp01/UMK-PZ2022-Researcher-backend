package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.Date
import java.util.concurrent.TimeUnit

@RestController
class AuthController(@Autowired val userService: UserService) {


    @PostMapping("/login/{login}/{password}")
    fun checkUserDetails(@PathVariable login:String, @PathVariable password:String): ResponseEntity<String> {
            val user = userService.getUserByLogin(login)
            if (user !== User() ){
                if(BCrypt.checkpw(password,user.password)) try {
                    val payload = mapOf(Pair("username",user.login))
                    val expiryDate = Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10))
                    val algorithm = Algorithm.HMAC512(System.getenv("ACCESS_TOKEN_SECRET"))

                    val accessToken= JWT
                        .create()
                        .withPayload(payload)
                        .withExpiresAt(expiryDate)
                        .sign(algorithm)

                    return ResponseEntity.status(HttpStatus.OK).body(accessToken)

                }catch (error:Error){
                    return ResponseEntity.status(HttpStatus.OK).build()
                }
                return ResponseEntity.status(HttpStatus.OK).build()
            }
            return ResponseEntity.status(HttpStatus.OK).build()
        }



}