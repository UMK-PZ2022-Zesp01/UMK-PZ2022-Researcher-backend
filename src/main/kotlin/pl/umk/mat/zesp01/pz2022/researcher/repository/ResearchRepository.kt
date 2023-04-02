package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import java.util.*

@Repository
interface ResearchRepository : MongoRepository<Research, String> {
	fun findResearchByResearchCode(researchCode: String): Optional<Research>

	@Query("{'researchCode': ?0}", delete = true)
	fun deleteResearchByResearchCode(code: String): Optional<Research>

}