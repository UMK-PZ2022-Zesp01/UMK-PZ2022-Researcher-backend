package pl.umk.mat.zesp01.pz2022.researcher.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.QuestionRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class QuestionServiceTests {

    @Autowired
    lateinit var questionService: QuestionService
    @Autowired
    lateinit var questionRepository: QuestionRepository
    lateinit var questionTestObject: Question
    lateinit var testQuestionCode: String

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
    }


    @Test
    fun `should add new Question by QuestionService`() {
        // GIVEN (questionTestObject)

        // WHEN
        val addedQuestion = questionService.addQuestion(questionTestObject)

        questionTestObject = questionTestObject.copy(
            questionCode = addedQuestion.questionCode,
            addedDateTime = addedQuestion.addedDateTime
        )
        testQuestionCode = addedQuestion.questionCode

        // THEN
        assertTrue(questionTestObject == questionRepository.findQuestionByQuestionCode(testQuestionCode).get())
    }

    @Test
    fun `should get question by code by QuestionService`() {
        // GIVEN
        questionRepository.save(questionTestObject)

        // WHEN
        val question = questionService.getQuestionByQuestionCode(testQuestionCode).get()

        // THEN
        assertTrue(questionTestObject == question)
    }

    @Test
    fun `should get questions by ResearchCode by QuestionService`() {
        // GIVEN
        questionRepository.insert(questionTestObject)
        questionRepository.insert(questionTestObject.copy(questionCode = "OtherCode"))
        questionRepository.insert(
            questionTestObject.copy(
                questionCode = "OtherCode2",
                researchCode = "OtherResearchCODE"
            )
        )

        // WHEN
        val questions = questionService.getQuestionsByResearchCode(questionTestObject.researchCode).get()

        // THEN
        assertTrue(2 == questions.size)
    }

    @Test
    fun `should update question by QuestionService`() {
        // GIVEN
        questionRepository.save(questionTestObject)

        questionTestObject = questionTestObject.copy(
            question = "Is this some other Question?",
            answer = "Maybe..."
        )

        // WHEN
        questionService.updateQuestion(
            questionTestObject,
            QuestionUpdateRequest(questionTestObject.question, questionTestObject.answer)
        )

        // THEN

        assertEquals(questionTestObject, questionRepository.findQuestionByQuestionCode(testQuestionCode).get())

    }

    @Test
    fun `should delete question by QuestionService`() {
        // GIVEN
        questionRepository.save(questionTestObject)

        // WHEN
        questionService.deleteQuestion(testQuestionCode)

        // THEN
        assertTrue(questionRepository.findAll().isEmpty())
    }


}