package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository

@Service
class ResearchService(
    @Autowired val researchRepository: ResearchRepository,
    @Autowired val mongoOperations: MongoOperations
) {

    /*** ADD METHODS ***/

    fun addResearch(research: Research): Research = researchRepository.insert(research)

    /*** DELETE METHODS ***/

    fun deleteResearchById(id: String) = researchRepository.deleteById(id)

    /*** GET METHODS ***/

    fun getAllResearches(): List<Research> = researchRepository.findAll()

    fun getResearchById(id: String): Research =
        researchRepository.findById(id)
            .orElseThrow { throw RuntimeException("Cannot find User by Id") }

    fun getResearchesByUserId(userId: String): List<Research> =
        researchRepository.findResearchesById(userId)
            .orElseThrow { throw RuntimeException("Cannot find User by Id") }

    fun sortResearchesByTitle(): List<Research> =
        mongoOperations.find(
            Query().with(Sort.by(Sort.Direction.ASC, "title")),
            Research::class.java
        )
}