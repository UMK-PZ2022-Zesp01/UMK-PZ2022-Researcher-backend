package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.CodeGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository

@Service
class ResearchService(
	@Autowired val researchRepository: ResearchRepository,
	@Autowired val mongoOperations: MongoOperations
) {

	/*** ADD METHODS ***/

	fun addResearch(research: Research): Research {
		val updatedResearch = research.copy(
			researchCode = CodeGenerator().generateResearchCode(research.id)
		)
		return researchRepository.insert(updatedResearch)
	}

	fun updateResearch(research: Research, updateData: ResearchUpdateRequest) {
		val updatedResearch = research.copy(
			title = updateData.title.ifEmpty { research.title },
			description = updateData.description.ifEmpty { research.description },

			participantLimit = if (updateData.participantLimit != research.participantLimit)
				updateData.participantLimit
			else research.participantLimit,

			location = if (updateData.location != research.location)
				updateData.location
			else research.location
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("researchCode").`is`(research.researchCode)),
			updatedResearch
		)
	}

	/*** DELETE METHODS ***/

	fun deleteResearchById(id: String) =
		researchRepository.deleteById(id)

	/*** GET METHODS ***/

	fun getAllResearches(): List<Research> =
		researchRepository.findAll()

	fun getResearchById(id: String): Research =
		researchRepository.findById(id)
			.orElseThrow { throw RuntimeException("Cannot find User by Id") }

	fun getResearchesByCreatorId(creatorId: String): List<Research> =
		mongoOperations.find(
			Query().addCriteria(Criteria.where("creatorId").`is`(creatorId)),
			Research::class.java
		)

	fun getResearchesByCreatorLogin(creatorLogin: String): List<Research> =
		mongoOperations.find(
			Query().addCriteria(Criteria.where("creatorLogin").`is`(creatorLogin)),
			Research::class.java
		)


	fun sortResearchesByTitle(): List<Research> =
		mongoOperations.find(
			Query().with(Sort.by(Sort.Direction.ASC, "title")),
			Research::class.java
		)

	fun getAllResearchIds(): List<String> =
		mongoOperations.aggregate(
			Aggregation.newAggregation(
				Aggregation.project("_id")
			),
			"Researches", String::class.java
		).mappedResults
}