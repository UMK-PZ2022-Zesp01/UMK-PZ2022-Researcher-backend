package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("Questions")
data class Question(
	val questionCode: String = "",
	@Indexed(name = "questionResearchCodeIndex") val researchCode: String = "",
	val addedDateTime: String = "",
	val researchOwnerLogin: String = "",
	val authorLogin: String = "",
	val authorFullName: String = "",
	val question: String = "",
	val answer: String = "",
) {
	fun toQuestionResponse(): QuestionResponse =
		QuestionResponse(
			addedDateTime = addedDateTime,
			questionCode = questionCode,
			researchOwnerLogin = researchOwnerLogin,
			authorLogin = authorLogin,
			authorFullName = authorFullName,
			question = question,
			answer = answer
		)
}

class QuestionRequest(
	private val researchCode: String,
	private val researchOwnerLogin: String,
	private val authorLogin: String,
	private val authorFullName: String,
	private val question: String,
	private val answer: String
) {
	fun toQuestion(): Question =
		Question(
			researchCode = researchCode,
			researchOwnerLogin = researchOwnerLogin,
			authorLogin = authorLogin,
			authorFullName = authorFullName,
			question = question,
			answer = answer
		)
}

class QuestionUpdateRequest(
	val question: String? = null,
	val answer: String? = null,
)

class QuestionResponse(
	private val addedDateTime: String,
	private val questionCode: String,
	private val authorLogin: String,
	private val researchOwnerLogin: String,
	private val authorFullName: String,
	private val question: String,
	private val answer: String
)