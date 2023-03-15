package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.HttpStatusCode
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.VerificationToken
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.VERIFICATION_EXPIRES_SEC
import pl.umk.mat.zesp01.pz2022.researcher.service.VerificationTokenService
import java.net.URI
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerTests(
        @Autowired val restTemplate: TestRestTemplate,
        @Autowired val userRepository: UserRepository,
        @Autowired val verificationTokenService: VerificationTokenService,
        @Autowired val verificationTokenRepository: VerificationTokenRepository
) {

    @LocalServerPort
    private val port: Int = 3000

    lateinit var userTestObject: User

    @BeforeEach
    fun setup() {
        userTestObject = User(
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
            false)
        userRepository.deleteAll()
        verificationTokenService.deleteUserTokens(userTestObject)
    }

    @Test
    fun `add new User and returns CREATED (201)`() {
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
    fun `sendVerificationEmail when user exists and not confirmed and returns CREATED (201)`() {
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
    fun `sendVerificationEmail when user exists and is confirmed and returns NO_CONTENT (204)`() {
        // GIVEN
        userTestObject.isConfirmed = true
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?username=_testLOGIN", String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `sendVerificationEmail when user does not exist and returns NO_CONTENT (204)`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?username=unknown", String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `confirmAccount when user is already confirmed and returns NO_CONTENT (204)`() {
        // GIVEN
        userTestObject.isConfirmed = true
        userRepository.save(userTestObject)

        val token = verificationTokenService.createToken(userTestObject.login)
        val url = "/user/confirm?token=$token"

        // WHEN
        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertTrue(userRepository.findUserByLogin(userTestObject.login).get().isConfirmed)
    }

    @Test
    fun `confirmAccount when user is not confirmed and token is valid and returns CREATED (201) `() {
        // GIVEN
        userRepository.save(userTestObject)

        val token = verificationTokenService.createToken(userTestObject.login)
        val url = "/user/confirm?token=$token"

        // WHEN
        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        // THEN
        assertEquals(CREATED, response.statusCode)
        assertTrue(userRepository.findUserByLogin(userTestObject.login).get().isConfirmed)
    }

    @Test
    fun `confirmAccount when token is invalid and returns UNAUTHORIZED (401)`() {
        // GIVEN
        userRepository.save(userTestObject)

        val token = "invalid-token"
        val url = "/user/confirm?token=$token"

        // WHEN
        val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)

        // THEN
        assertEquals(UNAUTHORIZED, response.statusCode)
    }

}