package pl.umk.mat.zesp01.pz2022.researcher.controller.question

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.repository.QuestionRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.ReportRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ReportControllerTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val reportRepository: ReportRepository,
    @Autowired val refreshTokenService: RefreshTokenService
) {
    lateinit var reportTestObject: Report
    lateinit var userTestObject: User
    lateinit var testReportCode: String
    lateinit var validJwt: String

    @BeforeEach
    fun setup() {
        reportRepository.deleteAll()
        reportTestObject = Report(
            reportCode = "TestReportCODE",
            reportMessage = "Hello, i would like to report TEST MESSAGE"
        )
        testReportCode = reportTestObject.reportCode
        userTestObject = User(
            login = "authorLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            location = "Bydgoszcz",
            isConfirmed = true
        )
        userRepository.save(userTestObject)
        validJwt = refreshTokenService.createAccessToken(userTestObject.login)
    }

    @Test
    fun `test addReport with valid token should return 201 CREATED`() {
        // GIVEN (reportTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validJwt

        val request = HttpEntity(reportTestObject, headers)
        val response = restTemplate.postForEntity("/report/send", request, Void::class.java)

        // THEN
        assertEquals(CREATED, response.statusCode)
        assertTrue(reportRepository.findAll().size == 1)
    }

    @Test
    fun `test addReport with invalid token should return 403 FORBIDDEN`() {
        // GIVEN (reportTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = "INVALID-JWT"

        val request = HttpEntity(reportTestObject, headers)
        val response = restTemplate.postForEntity("/report/send", request, Void::class.java)

        // THEN
        assertEquals(FORBIDDEN, response.statusCode)
        assertTrue(reportRepository.findAll().isEmpty())
    }

    @Test
    fun `test deleteReport with valid token should return 204 NO_CONTENT`() {
        // GIVEN
        reportRepository.save(reportTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validJwt

        val response = restTemplate.exchange("/report/$testReportCode/delete", HttpMethod.DELETE, HttpEntity(null, headers), String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertTrue(reportRepository.findAll().isEmpty())
    }

    @Test
    fun `test deleteReport with invalid token should return 403 FORBIDDEN`() {
        // GIVEN
        reportRepository.save(reportTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = "INVALID-JWT"

        val response = restTemplate.exchange("/report/$testReportCode/delete", HttpMethod.DELETE, HttpEntity(null, headers), String::class.java)

        // THEN
        assertEquals(FORBIDDEN, response.statusCode)
        assertTrue(reportRepository.findAll().isNotEmpty())
    }
}