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
    fun `add new User`() {
        // GIVEN
        val userTestObject = User(
            "_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG",
            true
        )

        // WHEN
        val response = restTemplate.exchange("/user/add", POST, HttpEntity(userTestObject), String::class.java)

        // THEN
        assertThat(response.statusCode).isEqualTo(CREATED)
    }

    @Test
    fun `add user whose email is already in the database`() {
        // GIVEN
        val userTestObject = User(
            "_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG",
            true
        )
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.exchange("/user/add", POST, HttpEntity(userTestObject), String::class.java)

        // THEN
        assertThat(response.statusCode.value()).isEqualTo(299)
    }



}