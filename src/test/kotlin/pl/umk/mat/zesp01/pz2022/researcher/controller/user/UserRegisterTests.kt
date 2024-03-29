package pl.umk.mat.zesp01.pz2022.researcher.controller.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.RefreshTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserRegisterTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
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
            isConfirmed = false
        )
        testUserLogin = userTestObject.login
        userRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        verificationTokenRepository.deleteAll()
    }

    @Test
    fun `add new User and returns CREATED (201)`() {
        // GIVEN (userTestObject)

        // WHEN
        val result = restTemplate.exchange<String>("/user/register", HttpMethod.POST, HttpEntity(userTestObject, null))

        // THEN
        assertEquals(CREATED, result.statusCode)
        assertTrue(userRepository.findUserByLogin(testUserLogin).isPresent)
    }

    @Test
    fun `add user whose email is already in the database and returns 299`() {
        // GIVEN (userTestObject)
        val userTestObject2 = User(
            login = "testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
//            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )

        userRepository.save(userTestObject)

        // WHEN
        val result = restTemplate.exchange<String>("/user/register", HttpMethod.POST, HttpEntity(userTestObject2, null))

        // THEN
        assertEquals(HttpStatusCode.valueOf(299), result.statusCode)
    }

    @Test
    fun `add user whose login is already in the database and returns 298`() {
        // GIVEN (userTestObject)
        val userTestObject2 = User(
            login = "testLOGIN",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
//            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )
        userRepository.save(userTestObject)

        // WHEN
        val result = restTemplate.exchange<String>("/user/register", HttpMethod.POST, HttpEntity(userTestObject2, null))


        // THEN
        assertEquals(HttpStatusCode.valueOf(298), result.statusCode)
    }

}