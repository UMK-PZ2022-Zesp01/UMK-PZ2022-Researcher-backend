package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import com.google.gson.JsonElement
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
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.VerificationTokenService
import java.net.URI
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerTests(
        @Autowired val restTemplate: TestRestTemplate,
        @Autowired val userRepository: UserRepository,
        @Autowired val verificationTokenService: VerificationTokenService,
        @Autowired val refreshTokenService: RefreshTokenService
) {

    @LocalServerPort
    private val port: Int = 3000

    lateinit var userTestObject: User
    lateinit var testUserID: String
    lateinit var testUserLogin: String

    @BeforeEach
    fun setup() {
        userTestObject = User(
            id = "_testID",
            login = "_testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false)
        testUserID = userTestObject.id
        testUserLogin = userTestObject.login
        userRepository.deleteAll()
        verificationTokenService.deleteUserTokens(userTestObject)
        refreshTokenService.deleteToken(testUserID)
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
            id = "_testID2",
            login = "_testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
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
            id = "_testID2",
            login = "_testLOGIN",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
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

    @Test
    fun `updateUser should update user with given ID and returns OK (200)`() {
        // GIVEN
        val userTestObject2 = User(
            id = "_testID2",
            login = "_testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )
        userRepository.save(userTestObject)


        // WHEN
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(userTestObject2, headers)
        val response = restTemplate.exchange<User>("/user/$testUserID/update", HttpMethod.PUT, request)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(testUserID, response.body?.id)
        assertEquals(userTestObject2.login, response.body?.login)
        assertNotEquals(userTestObject2.password, response.body?.password) //password should be hashed
        assertEquals(userTestObject2.firstName, response.body?.firstName)
        assertEquals(userTestObject2.lastName, response.body?.lastName)
        assertEquals(userTestObject2.email, response.body?.email)
        assertEquals(userTestObject2.phone, response.body?.phone)
        assertEquals(userTestObject2.birthDate, response.body?.birthDate)
        assertEquals(userTestObject2.gender, response.body?.gender)
        assertEquals(userTestObject2.avatarImage, response.body?.avatarImage)

        // Verify that user was actually updated in the database
        val updatedUserResponse = restTemplate.getForEntity("/user/$testUserID", User::class.java)
        assertEquals(OK, updatedUserResponse.statusCode)
        assertEquals(testUserID, updatedUserResponse.body?.id)
        assertEquals(userTestObject2.login, updatedUserResponse.body?.login)
        assertNotEquals(userTestObject2.password, updatedUserResponse.body?.password) //password should be hashed
        assertEquals(userTestObject2.firstName, updatedUserResponse.body?.firstName)
        assertEquals(userTestObject2.lastName, updatedUserResponse.body?.lastName)
        assertEquals(userTestObject2.email, updatedUserResponse.body?.email)
        assertEquals(userTestObject2.phone, updatedUserResponse.body?.phone)
        assertEquals(userTestObject2.birthDate, updatedUserResponse.body?.birthDate)
        assertEquals(userTestObject2.gender, updatedUserResponse.body?.gender)
        assertEquals(userTestObject2.avatarImage, updatedUserResponse.body?.avatarImage)
    }

    @Test
    fun `getUserProfile with valid token and returns OK (200)`() {
        // GIVEN
        userRepository.save(userTestObject)
        val validToken = refreshTokenService.createAccessToken(userTestObject.login)

        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", validToken)
        val response = restTemplate.exchange<String>("/user/current", HttpMethod.GET, HttpEntity(null, headers))

        // THEN
        assertEquals(OK, response.statusCode)
        //TODO test the response body
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

    @Test
    fun `getPhoneByUserLogin for correct login and returns OK (200)`(){
        // GIVEN
        userRepository.save(userTestObject)
        val expectedPhoneNumber = userTestObject.phone

        // WHEN
        val response = restTemplate.exchange<String>("/getPhoneByUserLogin/$testUserLogin", HttpMethod.GET, null)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(expectedPhoneNumber, response.body)
    }

    @Test
    fun `getPhoneByUserLogin for invalid login and returns NO_CONTENT (204)`(){
        // WHEN
        val response = restTemplate.exchange<String>("/getPhoneByUserLogin/invalid-login", HttpMethod.GET, null)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
    }


    @Test
    fun `getAllUsers and returns OK (200)`() {
        // GIVEN
        val userTestObject2 = User(
            id = "_testID2",
            login = "_testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )
        val expectedUsers: List<User> = listOf(userTestObject, userTestObject2)

        userRepository.saveAll(expectedUsers)


        // WHEN
        val response = restTemplate.exchange<List<User>>("/users", HttpMethod.GET, null)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(expectedUsers, response.body)
    }

    @Test
    fun `getAllUserIDs and returns OK (200)`() {
        // GIVEN
        val userTestObject2 = User(
            id = "_testID2",
            login = "_testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )
        val expectedUserIDs = listOf("{\"_id\": \"_testID\"}","{\"_id\": \"_testID2\"}")

        userRepository.saveAll(listOf(userTestObject, userTestObject2))


        // WHEN
        val response = restTemplate.exchange<List<String>>("/users/idList", HttpMethod.GET, null)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(expectedUserIDs, response.body)

    }

    @Test
    fun `getUserById with valid id and returns OK (200)`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.exchange<User>("/user/$testUserID", HttpMethod.GET, null)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(userTestObject, response.body)
    }

    @Test
    fun `getUserById with invalid id and returns NO_CONTENT (204)`() {
        // WHEN
        val response = restTemplate.exchange<User>("/user/invalid-id", HttpMethod.GET, null)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
    }

    @Test
    fun `getUserByEmail with valid email and returns OK (200)`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.exchange<User>("/user/email/${userTestObject.email}", HttpMethod.GET, null)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(userTestObject, response.body)
    }

    @Test
    fun `getUserByEmail with invalid email and returns NO_CONTENT (204)`() {
        // WHEN
        val response = restTemplate.exchange<User>("/user/email/invalid-email", HttpMethod.GET, null)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
    }

    @Test
    fun `deleteUserById and returns NO_CONTENT (204)`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val response = restTemplate.exchange<String>("/user/$testUserID/delete", HttpMethod.DELETE, null)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertTrue(userRepository.findById(testUserID).isEmpty)
    }


}