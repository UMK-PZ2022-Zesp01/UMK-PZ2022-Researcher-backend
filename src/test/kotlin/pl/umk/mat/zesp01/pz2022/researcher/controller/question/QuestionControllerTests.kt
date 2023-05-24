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
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class QuestionControllerTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val questionRepository: QuestionRepository,
    @Autowired val refreshTokenService: RefreshTokenService
) {
    lateinit var questionTestObject: Question
    lateinit var userTestObject: User
    lateinit var testQuestionCode: String
    lateinit var validJwt: String

    @BeforeEach
    fun setup() {
        questionRepository.deleteAll()
        questionTestObject = Question(
            questionCode = "testQuestionCODE",
            researchCode = "testResearchCODE",
            addedDateTime = "2023-01-01T13:37:55.651474700",
            researchOwnerLogin = "researchOwnerLOGIN",
            authorLogin = "authorLOGIN",
            authorFullName = "authorFULLNAME",
            question = "Is this the correct test question?",
            answer = "Yes, it is."
        )
        testQuestionCode = questionTestObject.questionCode
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
    fun `test addQuestion should return 201 CREATED`() {
        // GIVEN (questionTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validJwt

        val request = HttpEntity(questionTestObject, headers)
        val response = restTemplate.postForEntity("/question/send", request, Void::class.java)

        // THEN
        assertEquals(CREATED, response.statusCode)
    }

    @Test
    fun `getQuestionsByResearchCode should return a list of questions`() {
        // GIVEN
        questionRepository.saveAll(listOf(questionTestObject, questionTestObject.copy(questionCode = "otherCode")))

        // WHEN
        val response = restTemplate.getForEntity(
            "/question/find/research/${questionTestObject.researchCode}",
            List::class.java,
            HttpEntity(null, HttpHeaders())
        )

        // THEN
        assertEquals(OK, response.statusCode)
        assertTrue(response.body?.size == 2)
    }

    @Test
    fun `test updateQuestion should return 200 OK`() {
        // GIVEN
        val question = "Is this the new question?"
        val answer = "Maybe..."
        val questionUpdateRequest = QuestionUpdateRequest(question, answer)

        questionRepository.save(questionTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validJwt

        val request = HttpEntity(questionUpdateRequest, headers)
        val response =
            restTemplate.exchange("/question/$testQuestionCode/update", HttpMethod.PUT, request, String::class.java)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(question, questionRepository.findQuestionByQuestionCode(testQuestionCode).get().question)
        assertEquals(answer, questionRepository.findQuestionByQuestionCode(testQuestionCode).get().answer)
    }

    @Test
    fun `test deleteQuestion should return 204 NO_CONTENT`() {
        // GIVEN
        questionRepository.save(questionTestObject)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validJwt

        val response = restTemplate.exchange(
            "/question/$testQuestionCode/delete",
            HttpMethod.DELETE,
            HttpEntity(null, headers),
            String::class.java
        )

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
    }


}