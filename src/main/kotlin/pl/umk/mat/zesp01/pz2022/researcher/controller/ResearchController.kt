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
	/*** POST MAPPINGS ***/

	@PostMapping(value = ["/research/add"], consumes = ["multipart/form-data"])
	fun addResearch(
		@RequestPart("researchProperties") researchRequest: ResearchRequest,
		@RequestPart("posterImage") posterImage: MultipartFile
	): ResponseEntity<String> {
		val research = researchRequest.toResearch(posterImage)
		researchService.addResearch(research)
		return ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(research.researchCode))
	}

	/*** PUT MAPPINGS ***/

	@PutMapping("/research/{code}/update")
	fun updateResearch(
		@PathVariable code: String,
		@RequestBody researchUpdateData: ResearchUpdateRequest
	): ResponseEntity<String> {
		val research = researchRepository.findResearchByResearchCode(code).get()
		researchService.updateResearch(research, researchUpdateData)
		return ResponseEntity.status(HttpStatus.OK).build()
	}

	/*** GET MAPPINGS ***/

	@GetMapping("/researches")
	fun getAllResearches(): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())


	@GetMapping("/research/creatorId/{creatorId}")
	fun getResearchByUserId(@PathVariable creatorId: String): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchesByCreatorId(creatorId))

	@GetMapping("/research/creatorLogin/{creatorLogin}")
	fun getResearchByUserLogin(@PathVariable creatorLogin: String): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchesByCreatorLogin(creatorLogin))

	@GetMapping("/researches/sorted")
	fun getSortedResearches(): ResponseEntity<List<Research>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.sortResearchesByTitle())

	@GetMapping("/researches/idList")
	fun getAllResearchIds(): ResponseEntity<List<String>> =
		ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearchCodes())

	/*** DELETE MAPPINGS ***/

	@DeleteMapping("/research/{researchCode}/delete")
	fun deleteResearchByLogin(@PathVariable researchCode: String): ResponseEntity<String> {
		researchService.deleteResearchByResearchCode(researchCode)
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
	}
}