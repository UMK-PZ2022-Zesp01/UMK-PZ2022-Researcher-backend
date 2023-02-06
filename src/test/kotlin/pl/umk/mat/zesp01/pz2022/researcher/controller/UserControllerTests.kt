package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.UserProfileDTO
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerTests(
        @Autowired val restTemplate: TestRestTemplate,
        @Autowired val userRepository: UserRepository
) {

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun shouldAddUserCorrectly() {
        // given
        val userToAdd = User(login = "test")

        // when
        val response = restTemplate.exchange("/user/add", POST, HttpEntity(userToAdd), String::class.java)

        // then
        assertThat(response.statusCode).isEqualTo(CREATED)
    }

    @Test
    fun shouldNotAddUserWhenUserWithGivenEmailAlreadyExists() {
        // given
        val userToAdd = User(email = "www@o2.pl")
        userRepository.save(userToAdd)

        // when
        val response = restTemplate.exchange("/user/add", POST, HttpEntity(userToAdd), String::class.java)

        // then
        assertThat(response.statusCode.value()).isEqualTo(299)
    }

    @Test
    fun shouldGetUserProfileCorrectly() {
        // given
        val userToAdd = User(login = "test")
        userRepository.save(userToAdd)

        // and
        val accessToken = getAccessTokenFor(userToAdd.login)
        val httpHeaders = LinkedMultiValueMap<String, String>()
        httpHeaders["Authorization"] = listOf(accessToken)

        // when
        val response = restTemplate.exchange("/user/current", GET, HttpEntity<String>(httpHeaders), String::class.java)

        // then
        assertThat(response.statusCode.value()).isEqualTo(200)
        assertThat(response.body).isEqualTo(Gson().toJson(UserProfileDTO(login = "test")))
    }

    private fun getAccessTokenFor(userLogin: String): String {
        val payload = mapOf(Pair("username", userLogin))
        return JWT.create()
                .withPayload(payload)
                .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_EXPIRES_SEC * 1000))
                .sign(Algorithm.HMAC256(ACCESS_TOKEN_SECRET))
    }

}