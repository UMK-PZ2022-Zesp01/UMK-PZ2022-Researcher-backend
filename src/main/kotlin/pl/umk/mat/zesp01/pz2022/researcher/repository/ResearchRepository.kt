package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Research

@Repository
interface ResearchRepository : MongoRepository<Research, String> {
	fun findResearchByResearchCode(researchCode: String): Research

}