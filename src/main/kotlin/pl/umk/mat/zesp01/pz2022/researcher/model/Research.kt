package pl.umk.mat.zesp01.pz2022.researcher.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Researches")
class Research {
	@Id var id: String = ""
	//    @Field var creatorId: String = ""
	@Field var creatorLogin: String = ""
	@Field var title: String = ""
	@Field var description: String = ""
	@Field var posterId: String = ""
	@Field var participantLimit: Int = 0
	@Field var participants = listOf<User>()
	@Field var begDate: String = ""
	@Field var endDate: String = ""
	@Field var isActive: Boolean = false
	@Field var location: ResearchLocation? = null
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
				"cash" -> ResearchReward(reward.type, reward.value.toString().toFloat())
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

//                "age" -> ResearchRequirement(req.type, req.criteria as ResearchRequirementAge)

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

				else -> ResearchRequirement("", "")
			}
		}

		return research
	}
}

class ResearchLocation(
	var form: String = "",
	var place: String = ""
)

class ResearchReward(
	var type: String = "",
	var value: Any
)

class ResearchRequirement(
	var type: String = "",
	var criteria: Any
)

class ResearchRequirementAgeInterval(
	var ageMin: Int? = null,
	var ageMax: Int? = null
)

class ResearchRequirementOther(
	var description: String
)