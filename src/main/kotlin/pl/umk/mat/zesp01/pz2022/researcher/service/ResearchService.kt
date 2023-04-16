package pl.umk.mat.zesp01.pz2022.researcher.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.ResearchUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import java.util.*

@Service
class ResearchService(
	@Autowired val researchRepository: ResearchRepository,
	@Autowired val mongoOperations: MongoOperations
) {

	fun addResearch(research: Research): Research {
		val updatedResearch = research.copy(
			researchCode = RandomStringUtils.random(8, true, true)
		)
		return researchRepository.insert(updatedResearch)
	}

	fun updateResearch(research: Research, updateData: ResearchUpdateRequest) {
		val updatedResearch = research.copy(
			title = updateData.title ?: research.title,
			description = updateData.description ?: research.description,
			participantLimit = updateData.participantLimit ?: research.participantLimit,
			location = updateData.location ?: research.location
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("researchCode").`is`(research.researchCode)),
			updatedResearch
		)
	}

	fun addUserToParticipantsList(researchCode: String, login: String): String {
		val research = researchRepository.findResearchByResearchCode(researchCode).get()
		val participants: MutableList<String> = research.participants.toMutableList()

		if(participants.contains(login)) return "ERR_ALREADY_IN_LIST"

		participants.add(login)
		val updatedResearch = research.copy(participants = participants)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("researchCode").`is`(researchCode)),
			updatedResearch
		)
		return "OK"
	}

	fun getAllResearches(): List<Research> =
		researchRepository.findAll()

	fun getResearchByCode(code: String): Optional<Research> =
		researchRepository.findResearchByResearchCode(code)

	fun getResearchesByCreatorLogin(creatorLogin: String): List<Research> =
		mongoOperations.find(
			Query().addCriteria(Criteria.where("creatorLogin").`is`(creatorLogin)),
			Research::class.java
		)

//	fun sortResearchesByTitle(): List<Research> =
//		mongoOperations.find(
//			Query().with(Sort.by(Sort.Direction.ASC, "title")),
//			Research::class.java
//		)

	fun deleteResearchById(id: String) =
		researchRepository.deleteById(id)
}