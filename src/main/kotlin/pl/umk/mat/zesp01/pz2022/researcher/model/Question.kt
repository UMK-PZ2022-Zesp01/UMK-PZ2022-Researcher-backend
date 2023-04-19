package pl.umk.mat.zesp01.pz2022.researcher.model


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Questions")
data class Question(

        @Id var _id: String? = null,
        @Field var researchOwnerLogin: String = "",
        @Field var researchCode: String = "",
        @Field var question: String="",
        @Field var answer: String="",
)

class QuestionRequest(
        private val _id: String,
        private val researchOwnerLogin:String,
        private val researchCode: String,
        private val question: String,
        private val answer: String
        ){
    fun toQuestion(): Question {
        return Question(
                _id=_id,
                researchOwnerLogin = researchOwnerLogin,
                researchCode = researchCode,
                question=question,
                answer=answer
        )
    }}
data class QuestionUpdateRequest(
        val researchOwnerLogin: String? = null,
        val researchCode: String? = null,
        val question: String? = null,
        val answer: String? = null,
)