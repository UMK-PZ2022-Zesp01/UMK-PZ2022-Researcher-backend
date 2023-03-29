package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchResponse
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import java.util.*

@Repository
interface ResearchRepository : MongoRepository<Research, String> {
    @Query("{'title': ?0}")
    fun findResearchByTitle(title: String): Optional<Research>

    @Query("{'_id': ?0}")
    fun findResearchById(id: String): Optional<Research>
}