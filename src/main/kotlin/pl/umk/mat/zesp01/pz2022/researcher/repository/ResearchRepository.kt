package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchFilters

import java.util.*

@Repository
interface ResearchRepository : MongoRepository<Research, String> {
    fun findResearchByResearchCode(researchCode: String): Optional<Research>
    fun findAllByCreatorLogin(creatorLogin: String): Optional<List<Research>>
    fun deleteResearchByResearchCode(researchCode: String)
}