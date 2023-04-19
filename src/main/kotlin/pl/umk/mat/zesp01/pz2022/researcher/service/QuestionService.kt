package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.QuestionRepository
import java.util.Optional

@Service
class QuestionService(@Autowired val questionRepository: QuestionRepository,
                      @Autowired val mongoOperations: MongoOperations
) {
    fun addQuestion(question: Question) = questionRepository.insert(question)
    fun getQuestionsByResearchCode(code: String): List<Question> =
            mongoOperations.find(Query().addCriteria(Criteria.where("researchCode").`is`(code)),
                    Question::class.java
            )

    fun getQuestionById(id:String): Optional<Question> = questionRepository.findQuestionBy_id(id)


    fun sendQuestionAnswer(question: Question, questionData: QuestionUpdateRequest): String {
        val updatedQuestion = question.copy(
                researchOwnerLogin = questionData.researchOwnerLogin ?: question.researchOwnerLogin,
                researchCode=questionData.researchCode?:question.researchCode,
                question = questionData.question ?: question.question,
                answer=questionData.answer?:question.answer
                )

        mongoOperations.findAndReplace(
                Query.query(Criteria.where("_id").`is`(question._id)),
                updatedQuestion
        )
        return "ok"
    }

}