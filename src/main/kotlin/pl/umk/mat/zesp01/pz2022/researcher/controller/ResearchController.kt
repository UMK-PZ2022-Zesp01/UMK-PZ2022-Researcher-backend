package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService

@RestController
class ResearchController(@Autowired val researchService: ResearchService) {
    /* ___________________________________POST MAPPINGS___________________________________*/
    @PostMapping("/addResearch")
    fun addResearch(@RequestBody research: Research): ResponseEntity<String> {
        research.id = IdGenerator().generateResearchId()
        researchService.addResearch(research)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /* ___________________________________GET MAPPINGS___________________________________*/
    @GetMapping("/getAllResearches")
    fun getAllResearches(): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())

    @GetMapping("/getResearchById/{id}")
    fun getResearchById(@PathVariable id: String): ResponseEntity<Research> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchById(id))

    @GetMapping("/getResearchByUserId/{userId}")
    fun getResearchByUserId(@PathVariable userId: String): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchesByUserId(userId))

    /* ___________________________________DELETE MAPPINGS___________________________________*/
    @DeleteMapping("/deleteResearchById/{id}")
    fun deleteResearchById(@PathVariable id: String): ResponseEntity<String> {
        researchService.deleteResearchById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}