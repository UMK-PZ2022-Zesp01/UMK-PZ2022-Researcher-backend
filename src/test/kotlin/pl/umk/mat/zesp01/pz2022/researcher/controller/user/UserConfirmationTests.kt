package pl.umk.mat.zesp01.pz2022.researcher.controller.user

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.RefreshTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.FRONT_URL
import pl.umk.mat.zesp01.pz2022.researcher.service.VerificationTokenService
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserConfirmationTests(
    @Autowired var restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val verificationTokenService: VerificationTokenService,
    @Autowired val verificationTokenRepository: VerificationTokenRepository,
    @Autowired val refreshTokenRepository: RefreshTokenRepository
) {

    lateinit var userTestObject: User
    lateinit var testUserLogin: String

    @BeforeEach
    fun setup() {
        userTestObject = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            location = "Bydgoszcz",
            isConfirmed = false)
        testUserLogin = userTestObject.login
        userRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        verificationTokenRepository.deleteAll()

    }

    @Test
    fun `send verification email when user profile is not confirmed`() {
        // GIVEN
        val userTestObject = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
//            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false)

        userRepository.deleteAll()
        userRepository.save(userTestObject)

        // WHEN
        val responseEntity = restTemplate.getForEntity(
            "/user/sendVerificationMail?login=${userTestObject.login}",
            String::class.java
        )

        // THEN
        greenMail.waitForIncomingEmail(15000, 1)

        val receivedMail = greenMail.receivedMessages[0]

        assertEquals(HttpStatusCode.valueOf(201), responseEntity.statusCode)
        assertEquals(1, receivedMail.allRecipients.size)
        assertEquals("testEMAIL@test.com", receivedMail.allRecipients[0].toString())
        assertEquals("noreply@justresearch.pz2022.gmail.com", receivedMail.from[0].toString())
        assertEquals("JustResearch | Potwierdzenie rejestracji", receivedMail.subject)

        assertTrue(GreenMailUtil.getBody(receivedMail).contains(FRONT_URL))
    }

    @Test
    fun `sendVerificationEmail when user exists and not confirmed and returns CREATED (201)`() {
        // GIVEN
        userRepository.save(userTestObject)
        val quotedEmail: String = "\"" + userTestObject.email + "\"" // body from response is in quote

        // WHEN

        val response = restTemplate.getForEntity("/user/sendVerificationMail?login=testLOGIN", String::class.java)

        // THEN
        assertEquals(CREATED, response.statusCode)
        assertEquals(quotedEmail, response.body)
    }

    @Test
    fun `sendVerificationEmail when user exists and is confirmed and returns NO_CONTENT (204)`() {
        // GIVEN
        userTestObject = userTestObject.copy(isConfirmed = true)
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?login=_testLOGIN", String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `sendVerificationEmail when user does not exist and returns NO_CONTENT (204)`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.getForEntity("/user/sendVerificationMail?login=unknown", String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `confirmAccount when user is already confirmed and returns NO_CONTENT (204)`() {
        // GIVEN
        userTestObject = userTestObject.copy(isConfirmed = true)
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

    companion object {
        @JvmField
        @RegisterExtension
        var greenMail: GreenMailExtension = GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("username", "secret"))
            .withPerMethodLifecycle(true)
    }
}
