package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatusCode
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerTests(
        @Autowired val restTemplate: TestRestTemplate,
        @Autowired val userRepository: UserRepository
) {

    @LocalServerPort
    private val port: Int = 3000

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

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
        false
    )



    @Test
    fun `add new User and returns 201`() {
        // GIVEN (userTestObject)
        // WHEN
        val request = RequestEntity.post(URI("http://localhost:$port/user/register")).body(userTestObject)
        val result = restTemplate.exchange(request, String::class.java)

        // THEN
        assertEquals(CREATED, result.statusCode)
    }

    @Test
    fun `add user whose email is already in the database and returns 299`() {
        // GIVEN (userTestObject)
        val userTestObject2 = User(
            "_testID2",
            "_testLOGIN2",
            "testPASSWORD2",
            "testFIRSTNAME2",
            "testLASTNAME2",
            "testEMAIL@test.com",
            "1234567892",
            "01-01-19702",
            "Male2",
            "testAVATARIMAGE.IMG2",
            false
        )
        userRepository.save(userTestObject)

        // WHEN
        val request = RequestEntity.post(URI("http://localhost:$port/user/register")).body(userTestObject2)
        val result = restTemplate.exchange(request, String::class.java)

        // THEN
        assertEquals(HttpStatusCode.valueOf(299), result.statusCode)
    }

    @Test
    fun `add user whose login is already in the database and returns 298`() {
        // GIVEN (userTestObject)
        val userTestObject2 = User(
            "_testID2",
            "_testLOGIN",
            "testPASSWORD2",
            "testFIRSTNAME2",
            "testLASTNAME2",
            "testEMAIL@test.com2",
            "1234567892",
            "01-01-19702",
            "Male2",
            "testAVATARIMAGE.IMG2",
            false
        )
        userRepository.save(userTestObject)

        // WHEN
        val request = RequestEntity.post(URI("http://localhost:$port/user/register")).body(userTestObject2)
        val result = restTemplate.exchange(request, String::class.java)

        // THEN
        assertEquals(HttpStatusCode.valueOf(298), result.statusCode)
    }

    @Test
    fun `sendVerificationEmail when user exists and not confirmed and returns CREATED`() {
        // GIVEN
        userRepository.save(userTestObject)
        val quotedEmail: String = "\"" + userTestObject.email + "\"" // body from response is in quote

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?username=_testLOGIN", String::class.java)

        // THEN
        assertEquals(CREATED, response.statusCode)
        assertEquals(quotedEmail, response.body)
    }

    @Test
    fun `sendVerificationEmail when user exists and is confirmed and returns NO_CONTENT`() {
        // GIVEN
        userTestObject.isConfirmed = true
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?username=_testLOGIN", String::class.java)

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `sendVerificationEmail when user does not exist and returns NO_CONTENT`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?username=unknown", String::class.java)

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }



}