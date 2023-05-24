package pl.umk.mat.zesp01.pz2022.researcher.controller.auth

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import pl.umk.mat.zesp01.pz2022.researcher.model.LoginData
import pl.umk.mat.zesp01.pz2022.researcher.model.RefreshToken
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.UserRegisterRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.RefreshTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.VerificationTokenRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import java.net.URI
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class AuthControllerTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val verificationTokenRepository: VerificationTokenRepository,
    @Autowired val refreshTokenService: RefreshTokenService,
    @Autowired val refreshTokenRepository: RefreshTokenRepository
) {

    lateinit var userTestObject: User
    lateinit var testUserLogin: String
    lateinit var testUserPlainPassword: String

    @BeforeEach
    fun setup() {
        testUserPlainPassword = "testPASSWORD"
        userTestObject = User(
            login = "testLOGIN",
            password = BCrypt.hashpw(testUserPlainPassword, BCrypt.gensalt()),
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
    fun `handleLogin should return CREATED and set cookie`() {
        // GIVEN
        userTestObject = userTestObject.copy(isConfirmed = true)
        userRepository.save(userTestObject)

        val loginData = LoginData(
            testUserLogin,
            testUserPlainPassword,
            true
        )

        // WHEN
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity(loginData, headers)
        val responseEntity = restTemplate.exchange<String>("/login", HttpMethod.POST, requestEntity)

        val gson = Gson()
        val jsonObject = gson.fromJson(responseEntity.body, Map::class.java)

        val jwt = responseEntity.headers.getFirst(HttpHeaders.SET_COOKIE)?.split(";")?.get(0)?.removePrefix("jwt=")
        val refreshToken: RefreshToken? = if (jwt.isNullOrEmpty())
            null
        else refreshTokenService.verifyRefreshToken(jwt)

        // THEN
        assertEquals(CREATED, responseEntity.statusCode)
        assertEquals(testUserLogin, jsonObject["username"] as String)
        assertNotNull(jwt)
        assertNotNull(refreshToken)
        assertEquals(testUserLogin, refreshToken?.username)
    }

    @Test
    fun `handleLogin should return UNAUTHORIZED when user is not in the database`() {
        // GIVEN
        val loginData = LoginData(
            testUserLogin,
            testUserPlainPassword,
            true
        )

        // WHEN
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity(loginData, headers)
        val responseEntity = restTemplate.exchange<String>("/login", HttpMethod.POST, requestEntity)


        // THEN
        assertEquals(UNAUTHORIZED, responseEntity.statusCode)
        assertTrue(responseEntity.body!!.contains("Login failed: User ${loginData.login} does not exist"))
    }

    @Test
    fun `handleLogin should return UNAUTHORIZED when password isn't valid`() {
        // GIVEN
        userTestObject = userTestObject.copy(isConfirmed = true)
        userRepository.save(userTestObject)

        val loginData = LoginData(
            testUserLogin,
            "invalidPASSWORD",
            true
        )

        // WHEN
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity(loginData, headers)
        val responseEntity = restTemplate.exchange<String>("/login", HttpMethod.POST, requestEntity)


        // THEN
        assertEquals(UNAUTHORIZED, responseEntity.statusCode)
        assertTrue(responseEntity.body!!.contains("Login failed: Wrong password"))
    }

    @Test
    fun `handleLogin should return FORBIDDEN when user isn't confirmed`() {
        // GIVEN
        userRepository.save(userTestObject)

        val loginData = LoginData(
            testUserLogin,
            testUserPlainPassword,
            true
        )

        // WHEN
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity(loginData, headers)
        val responseEntity = restTemplate.exchange<String>("/login", HttpMethod.POST, requestEntity)

        // THEN
        assertEquals(FORBIDDEN, responseEntity.statusCode)
        assertTrue(responseEntity.body!!.contains("Login failed: Account has not been activated"))
    }


    @Test
    fun `handleRefreshToken should return OK and access token`() {
        // GIVEN
        userTestObject = userTestObject.copy(isConfirmed = true)
        userRepository.save(userTestObject)


        val refreshToken = refreshTokenService.createRefreshToken(testUserLogin, 1000)
        val headers = HttpHeaders().apply {
            add(HttpHeaders.COOKIE, "jwt=$refreshToken")
        }

        // WHEN
        val requestEntity = HttpEntity(null, headers)
        val responseEntity = restTemplate.exchange<String>("/auth/refresh", HttpMethod.GET, requestEntity)

        val gson = Gson()
        val jsonObject = gson.fromJson(responseEntity.body, Map::class.java)


        // THEN
        assertEquals(OK, responseEntity.statusCode)
        assertEquals(testUserLogin, jsonObject["username"])
        assertNotNull(jsonObject["accessToken"])
    }


    @Test
    fun `handleRefreshToken should return FORBIDDEN when user is not in the database`() {
        // GIVEN

        val refreshToken = refreshTokenService.createRefreshToken(testUserLogin, 1000)
        val headers = HttpHeaders().apply {
            add(HttpHeaders.COOKIE, "jwt=$refreshToken")
        }

        // WHEN
        val requestEntity = HttpEntity(null, headers)
        val responseEntity = restTemplate.exchange<String>("/auth/refresh", HttpMethod.GET, requestEntity)
        val setCookieHeader = responseEntity.headers.getFirst(HttpHeaders.SET_COOKIE)

        // THEN
        assertEquals(FORBIDDEN, responseEntity.statusCode)
        assertNotNull(setCookieHeader)
        assertTrue(setCookieHeader!!.contains("Max-Age=0"))
    }

    @Test
    fun `handleLogout should return NO_CONTENT and delete cookie`() {
        // GIVEN
        val refreshToken = refreshTokenService.createRefreshToken(testUserLogin, 1000)
        val headers = HttpHeaders().apply {
            add(HttpHeaders.COOKIE, "jwt=$refreshToken")
        }

        // WHEN
        val requestEntity = HttpEntity(null, headers)
        val responseEntity = restTemplate.exchange<String>("/logout", HttpMethod.DELETE, requestEntity)

        // THEN
        assertEquals(NO_CONTENT, responseEntity.statusCode)

        val setCookieHeader = responseEntity.headers.getFirst(HttpHeaders.SET_COOKIE)
        assertNotNull(setCookieHeader)
        assertTrue(setCookieHeader!!.contains("Max-Age=0"))
    }

}