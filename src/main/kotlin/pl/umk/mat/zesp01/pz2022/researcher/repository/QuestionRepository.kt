package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import java.util.*

@Repository
interface QuestionRepository : MongoRepository<Question, String> {
	fun findQuestionByQuestionCode(questionCode: String): Optional<Question>
	fun findQuestionsByResearchCode(researchCode: String): Optional<List<Question>>
	fun deleteQuestionByQuestionCode(questionCode: String)
}