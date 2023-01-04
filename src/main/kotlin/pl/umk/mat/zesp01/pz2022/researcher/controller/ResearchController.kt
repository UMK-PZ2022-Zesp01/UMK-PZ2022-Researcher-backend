package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.*

@RestController
class ResearchController(@Autowired val researchService: ResearchService) {
    @PostMapping("/addResearch")
    fun addResearch(@RequestBody research: Research): ResponseEntity<String> {
        //research.id = UUID.randomUUID().toString()
        research.id = IdGenerator().generateResearchId()
        researchService.addResearch(research)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/getAllResearches")
    fun getAll(): ResponseEntity<List<Research>> =
            ResponseEntity.status(HttpStatus.OK).body(researchService.getAll())

    @GetMapping("/getResearchById/{id}")
    fun getResearchById(@PathVariable id: String): ResponseEntity<Research> =
            ResponseEntity.status(HttpStatus.OK).body(researchService.getResearchById(id))
}