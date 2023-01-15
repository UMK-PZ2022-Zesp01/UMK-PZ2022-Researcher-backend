package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService

@RestController
class ResearchController(
    @Autowired val researchService: ResearchService,
    @Autowired val researchRepository: ResearchRepository
) {
    /*** POST MAPPINGS ***/

    @PostMapping("/addResearch")
    fun addResearch(@RequestBody research: Research): ResponseEntity<String> {
        research.id = IdGenerator().generateResearchId()
        researchService.addResearch(research)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /*** PUT MAPPINGS ***/

    @PutMapping("/updateResearch/{id}")
    fun updateResearch(@PathVariable id: String, @RequestBody research: Research): ResponseEntity<Research> {
        val oldResearch = researchRepository.findById(id).orElse(null)
        research.id = oldResearch.id
        if (research.userId.isEmpty()) research.userId = oldResearch.userId
        if (research.participants.isEmpty()) research.participants = oldResearch.participants
        if (research.title.isEmpty()) research.title = oldResearch.title
        if (research.description.isEmpty()) research.description = oldResearch.description
        if (research.durationTimeInMinutes.toString().isEmpty())
            research.durationTimeInMinutes = oldResearch.durationTimeInMinutes
        if (research.begDate.isEmpty()) research.begDate = oldResearch.begDate
        if (research.endDate.isEmpty()) research.endDate = oldResearch.endDate
        if (research.isActive.toString().isEmpty()) research.isActive = oldResearch.isActive
        if (research.locationForm.isEmpty()) research.locationForm = oldResearch.locationForm
        if (research.researchPlace.isEmpty()) research.researchPlace = oldResearch.researchPlace
        if (research.researchImage.isEmpty()) research.researchImage = oldResearch.researchImage
        if (research.minAgeRequirement.toString().isEmpty()) research.minAgeRequirement = oldResearch.minAgeRequirement
        if (research.maxAgeRequirement.toString().isEmpty()) research.maxAgeRequirement = oldResearch.maxAgeRequirement
        if (research.participantLimit.toString().isEmpty()) research.participantLimit = oldResearch.participantLimit
        if (research.genderRequirement.isEmpty()) research.genderRequirement = oldResearch.genderRequirement
        if (research.reward.isEmpty()) research.reward = oldResearch.reward

        return ResponseEntity.status(HttpStatus.OK).body(researchRepository.save(research))

    }

    /*** GET MAPPINGS ***/

    @GetMapping("/getAllResearches")
    fun getAllResearches(): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())

    @GetMapping("/getResearchById/{id}")
    fun getResearchById(@PathVariable id: String): ResponseEntity<Research> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchById(id))

    @GetMapping("/getResearchByUserId/{userId}")
    fun getResearchByUserId(@PathVariable userId: String): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchesByUserId(userId))

    /*** DELETE MAPPINGS ***/

    @DeleteMapping("/deleteResearchById/{id}")
    fun deleteResearchById(@PathVariable id: String): ResponseEntity<String> {
        researchService.deleteResearchById(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}