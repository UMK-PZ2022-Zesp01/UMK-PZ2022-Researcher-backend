package pl.umk.mat.zesp01.pz2022.researcher.controller.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.DeleteRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.UserResponse
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.RefreshTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val verificationTokenRepository: VerificationTokenRepository,
    @Autowired val refreshTokenService: RefreshTokenService,
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
    fun `updateUser should update user and returns OK (200)`() {
        // GIVEN
        val userUpdateRequest = UserUpdateRequest(
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "234567890",
            location = "Warszawa"
        )
        userRepository.save(userTestObject)

        val validToken = refreshTokenService.createAccessToken(userTestObject.login)

        // WHEN
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        headers.add("Authorization", validToken)
        val request = HttpEntity(userUpdateRequest, headers)

        val response = restTemplate.exchange(
            "/user/current/update",
            HttpMethod.PUT,
            request,
            UserUpdateRequest::class.java
        )

        // THEN
        assertEquals(OK, response.statusCode)

        // Verify that user was actually updated in the database
        val updatedUser = userRepository.findUserByLogin(testUserLogin).get()
        assertNotEquals(userUpdateRequest.password, updatedUser.password) //password should be hashed
        assertEquals(userUpdateRequest.firstName, updatedUser.firstName)
        assertEquals(userUpdateRequest.lastName, updatedUser.lastName)
        assertEquals(userUpdateRequest.email, updatedUser.email)
        assertEquals(userUpdateRequest.phone, updatedUser.phone)
    }

    @Test
    fun `deleteUserByLogin and returns NO_CONTENT (204)`() {
        // GIVEN
        val plainPass = userTestObject.password
        userTestObject = userTestObject.copy(password = BCrypt.hashpw(userTestObject.password, BCrypt.gensalt()))
        userRepository.save(userTestObject)
        val validToken = refreshTokenService.createAccessToken(userTestObject.login)
        val deleteRequest = DeleteRequest(plainPass)

        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", validToken)

        val request = HttpEntity(deleteRequest, headers)

        // WHEN
        val response = restTemplate.exchange<String>("/user/current/delete", HttpMethod.DELETE, request, String::class)



        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertTrue(userRepository.findById(testUserLogin).isEmpty)
    }

    @Test
    fun `getUserProfile with valid token and returns OK (200)`() {
        // GIVEN
        userRepository.save(userTestObject)
        val validToken = refreshTokenService.createAccessToken(userTestObject.login)

        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", validToken)
        val response = restTemplate.exchange<UserResponse>("/user/current", HttpMethod.GET, HttpEntity(null, headers))

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(userTestObject.toUserResponse(), response.body)
    }

    @Test
    fun `getUserProfile with invalid token and returns FORBIDDEN (403)`() {
        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", "invalid-token")
        val response = restTemplate.exchange<String>("/user/current", HttpMethod.GET, HttpEntity(null, headers))

        // THEN
        assertEquals(FORBIDDEN, response.statusCode)
    }

    @Test
    fun `getUserProfile with missing token and returns UNAUTHORIZED (401)`() {
        // WHEN
        val response = restTemplate.exchange<String>("/user/current", HttpMethod.GET, HttpEntity(null, HttpHeaders()))

        // THEN
        assertEquals(UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `getUserProfile with token for not existing user and returns UNAUTHORIZED (401)`() {
        // GIVEN
        userRepository.save(userTestObject)
        val validToken = refreshTokenService.createAccessToken("notUserTestObjectLogin")

        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", validToken)

        // WHEN
        val response = restTemplate.exchange<String>("/user/current", HttpMethod.GET, HttpEntity(null, HttpHeaders()))

        // THEN
        assertEquals(UNAUTHORIZED, response.statusCode)
    }
}