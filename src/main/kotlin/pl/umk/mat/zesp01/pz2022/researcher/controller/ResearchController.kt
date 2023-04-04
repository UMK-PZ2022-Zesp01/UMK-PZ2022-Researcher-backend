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
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService
import kotlin.math.min

@RestController
class ResearchController(
    @Autowired val researchService: ResearchService,
) {

    @PostMapping(value = ["/research/add"], consumes = ["multipart/form-data"])
    fun addResearch(
        @RequestPart("researchProperties") researchRequest: ResearchRequest,
        @RequestPart("posterImage") posterImage: MultipartFile
    ): ResponseEntity<String> {
        val research = researchRequest.toResearch(posterImage)
        val addedResearch = researchService.addResearch(research)
        return ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(addedResearch.researchCode))
    }

    @PutMapping("/research/{code}/update")
    fun updateResearch(
        @PathVariable code: String,
        @RequestBody researchUpdateData: ResearchUpdateRequest
    ): ResponseEntity<String> {
        val research = researchService.getResearchByCode(code).get()
        researchService.updateResearch(research, researchUpdateData)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @GetMapping("/research/all")
    fun getAllResearches(): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())

    @GetMapping("/research/page/{page}/{perPage}", produces = ["application/json;charset=UTF-8"])
    fun getAPageOfResearches(
        @PathVariable page: Int,
        @PathVariable perPage: Int
    ): ResponseEntity<String> {
        val allResearches = researchService.getAllResearches()
        val length = allResearches.size

        if (page > 0 && perPage > 0) {
            val firstIndex = min(((page - 1) * perPage), length)
            val lastIndex = min((page * perPage), length)

            val responseBody = allResearches
                    .subList(firstIndex, lastIndex)
                    .map { research -> research.toResearchResponse() }

            return ResponseEntity
                .status(HttpStatus.OK)
                .body(Gson().toJson(responseBody))
        }

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    @GetMapping("/research/code/{code}")
    fun getResearchByCode(@PathVariable code: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.OK).body(
            Gson().toJson(researchService.getResearchResponseByCode(code))
        )

    @GetMapping("/research/creator/{creatorLogin}")
    fun getResearchByUserLogin(@PathVariable creatorLogin: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.OK).body(
            Gson().toJson(researchService.getResearchesByCreatorLogin(creatorLogin))
        )

//	@GetMapping("/research/all/sorted")
//	fun getSortedResearches(): ResponseEntity<List<Research>> =
//		ResponseEntity.status(HttpStatus.OK).body(researchService.sortResearchesByTitle())

    @DeleteMapping("/research/{code}/delete")
    fun deleteResearchById(@PathVariable code: String): ResponseEntity<String> {
        researchService.deleteResearchByResearchCode(code)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}