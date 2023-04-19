package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import java.util.Optional

@Repository
interface QuestionRepository : MongoRepository<Question, String> {
    fun findQuestionBy_id(id:String):Optional<Question>
}