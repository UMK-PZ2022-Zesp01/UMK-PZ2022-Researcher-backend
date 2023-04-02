package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService

@RestController
class ResearchController(
	@Autowired val researchService: ResearchService,
	@Autowired val researchRepository: ResearchRepository
) {

	@PostMapping(value = ["/research/add"], consumes = ["multipart/form-data"])
	fun addResearch(
		@RequestPart("researchProperties") researchRequest: ResearchRequest,
		@RequestPart("posterImage") posterImage: MultipartFile
	): ResponseEntity<String> {
		val research = researchRequest.toResearch(posterImage)
		researchService.addResearch(research)
		return ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(research.researchCode))
	}

	@PutMapping("/research/{code}/update")
	fun updateResearch(
		@PathVariable code: String,
		@RequestBody researchUpdateData: ResearchUpdateRequest
	): ResponseEntity<Research> {
		val research = researchService.getResearchByCode(code)
		researchService.updateResearch(research, researchUpdateData)
		return ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchByCode(code))
	}

	@GetMapping("/research/all")
	fun getAllResearches(): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())

	@GetMapping("/research/code/{code}")
	fun getResearchByCode(@PathVariable code: String): ResponseEntity<Research> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchByCode(code))

	@GetMapping("/research/creator/{creatorLogin}")
	fun getResearchByUserLogin(@PathVariable creatorLogin: String): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchesByCreatorLogin(creatorLogin))

//	@GetMapping("/research/all/sorted")
//	fun getSortedResearches(): ResponseEntity<List<Research>> =
//		ResponseEntity.status(HttpStatus.OK).body(researchService.sortResearchesByTitle())

	@DeleteMapping("/research/{code}/delete")
	fun deleteResearchById(@PathVariable code: String): ResponseEntity<String> {
		researchService.deleteResearchById(code)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}