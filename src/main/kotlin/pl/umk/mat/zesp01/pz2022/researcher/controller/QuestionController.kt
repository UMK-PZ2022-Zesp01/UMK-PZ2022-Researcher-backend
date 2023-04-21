package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.QuestionUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.service.QuestionService
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService

@RestController
class QuestionController(
	@Autowired val questionService: QuestionService,
	@Autowired val refreshTokenService: RefreshTokenService
) {
	@PostMapping("/question/send")
	fun addQuestion(
		@RequestBody questionData: QuestionRequest,
		@RequestHeader httpHeaders: HttpHeaders
	): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		return try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()

			val question = questionData.toQuestion()
			questionService.addQuestion(question)

			ResponseEntity.status(HttpStatus.CREATED).build()
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}

	@GetMapping("/question/find/research/{code}")
	fun getQuestionsByResearchCode(@PathVariable code: String): ResponseEntity<String> {
		val questions = questionService
			.getQuestionsByResearchCode(code)
			.get()
			.map { question -> question.toQuestionResponse() }

		return ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(questions))
	}

	@PutMapping("/question/{code}/update")
	fun updateQuestion(
		@PathVariable code: String,
		@RequestBody questionUpdateData: QuestionUpdateRequest,
		@RequestHeader httpHeaders: HttpHeaders
	): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		return try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()

			val question = questionService.getQuestionByQuestionCode(code).get()
			questionService.updateQuestion(question, questionUpdateData)

			ResponseEntity.status(HttpStatus.OK).build()
		} catch (e: Exception) {
			when (e) {
				is NoSuchElementException -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
				else -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
			}
		}
	}

	@DeleteMapping("/question/{code}/delete")
	fun deleteQuestion(
		@PathVariable code: String,
		@RequestHeader httpHeaders: HttpHeaders
	): ResponseEntity<String> {
		val jwt = httpHeaders["Authorization"]
			?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

		return try {
			val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
			if (username.isEmpty()) throw Exception()

			val question = questionService.getQuestionByQuestionCode(code).get()
			if (username != question.authorLogin) throw Exception()

			questionService.deleteQuestion(code)

			ResponseEntity.status(HttpStatus.NO_CONTENT).build()
		} catch (e: Exception) {
			ResponseEntity.status(HttpStatus.FORBIDDEN).build()
		}
	}
}