package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.Question
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.service.QuestionService
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService

@RestController
class QuestionController(@Autowired val questionService: QuestionService,
                         @Autowired val refreshTokenService: RefreshTokenService
) {
    @PostMapping("/questions/send")
    fun addQuestion(@RequestBody question: Question,@RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return try{
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()
            questionService.addQuestion(question)
            ResponseEntity.status(HttpStatus.CREATED).build()
        }catch (e:Exception){
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }


    @GetMapping("/questions/research/code/{code}")
    fun getQuestionsByResearchCode(@PathVariable code: String): ResponseEntity<List<Question>> =
            ResponseEntity.status(HttpStatus.OK).body(questionService.getQuestionsByResearchCode(code))

    @PutMapping("/questions/sendAnswer/id/{id}")
    fun sendQuestionAnswer(
            @PathVariable id:String ,
            @RequestBody questionUpdateData: QuestionUpdateRequest,
            @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String>{
        val jwt = httpHeaders["Authorization"]
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()


            val question=questionService.getQuestionById(id).get()
            questionService.sendQuestionAnswer(question,questionUpdateData)
            ResponseEntity.status(HttpStatus.OK).build()
        }catch (e:Exception){
            when (e) {
                is NoSuchElementException -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                else -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
    }

}