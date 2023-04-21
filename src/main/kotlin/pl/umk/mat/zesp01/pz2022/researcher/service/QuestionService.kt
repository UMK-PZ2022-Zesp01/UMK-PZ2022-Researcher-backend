package pl.umk.mat.zesp01.pz2022.researcher.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.QuestionRepository
import java.time.LocalDateTime
import java.util.*

@Service
class QuestionService(
	@Autowired val questionRepository: QuestionRepository,
	@Autowired val mongoOperations: MongoOperations
) {
	fun addQuestion(question: Question): Question {
		val updatedQuestion = question.copy(
			questionCode = RandomStringUtils.random(8, true, true),
			addedDateTime = LocalDateTime.now().toString(),
		)
		return questionRepository.insert(updatedQuestion)
	}

	fun getQuestionByQuestionCode(code: String): Optional<Question> =
		questionRepository.findQuestionByQuestionCode(code)

	fun getQuestionsByResearchCode(code: String): Optional<List<Question>> =
		questionRepository.findQuestionsByResearchCode(code)

	fun updateQuestion(question: Question, questionData: QuestionUpdateRequest) {
		val updatedQuestion = question.copy(
			question = questionData.question ?: question.question,
			answer = questionData.answer ?: question.answer
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("questionCode").`is`(question.questionCode)),
			updatedQuestion
		)
	}

//	fun sendQuestionAnswer(question: Question, questionData: QuestionUpdateRequest): String {
//		val updatedQuestion = question.copy(
//			question = questionData.question ?: question.question,
//			answer = questionData.answer ?: question.answer
//		)
//
//		mongoOperations.findAndReplace(
//			Query.query(Criteria.where("questionCode").`is`(question.questionCode)),
//			updatedQuestion
//		)
//		return "ok"
//	}

	fun deleteQuestion(code: String) =
		questionRepository.deleteQuestionByQuestionCode(code)
}