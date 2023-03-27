package pl.umk.mat.zesp01.pz2022.researcher.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Researches")
class Research {
	@Id var id: String = ""
	@Field var creatorLogin: String = ""
	@Field var title: String = ""
	@Field var description: String = ""
	@Field var posterId: String = ""
	@Field var participantLimit: Int = 0
	@Field var participants = listOf<User>()
	@Field var begDate: String = ""
	@Field var endDate: String = ""
	@Field var location = ResearchLocation()
	@Field var rewards = listOf<ResearchReward>()
	@Field var requirements = listOf<ResearchRequirement>()
}

class ResearchResponse(
	var id: String,
	var researchCreatorId: String
	// TODO
)

class ResearchRequest(
	private val title: String,
	private val description: String,
	private val creatorLogin: String,
	private val posterId: String,
	private val begDate: String,
	private val endDate: String,
	private val participantLimit: Int,
	private val location: ResearchLocation,
	private val rewards: List<ResearchReward>,
	private val requirements: List<ResearchRequirement>
) {
	fun toResearch(): Research {
		val research = Research()

		research.title = this.title
		research.description = this.description
		research.creatorLogin = this.creatorLogin
		research.posterId = this.posterId
		research.begDate = this.begDate
		research.endDate = this.endDate
		research.participantLimit = this.participantLimit
		research.location = this.location

		research.rewards = this.rewards.map { reward ->

			when (reward.type) {
				"cash" -> ResearchReward(reward.type, reward.value.toString().toInt())
				"item" -> ResearchReward(reward.type, reward.value as String)
				else -> ResearchReward("", "")
			}
		}

		research.requirements = this.requirements.map { req ->
			when (req.type) {
				"gender" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<String>>() {}
					)
				)

				"age" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<ResearchRequirementAgeInterval>>() {}
					)
				)

				"place" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<String>>() {}
					)
				)

				"education" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<String>>() {}
					)
				)

				"marital" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<String>>() {}
					)
				)

				"other" -> ResearchRequirement(
					req.type,
					ObjectMapper().convertValue(
						req.criteria,
						object : TypeReference<List<ResearchRequirementOther>>() {}
					)
				)

				else -> ResearchRequirement("", "")
			}
		}

		return research
	}
}

class ResearchLocation(
	val form: String = "",
	val place: String = ""
)

class ResearchReward(
	val type: String = "",
	val value: Any
)

class ResearchRequirement(
	val type: String = "",
	val criteria: Any
)

class ResearchRequirementAgeInterval(
	val ageMin: Int = 0,
	val ageMax: Int = 0
)

class ResearchRequirementOther(
	val type: String = "",
	val description: String = ""
)