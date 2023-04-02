package pl.umk.mat.zesp01.pz2022.researcher.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.web.multipart.MultipartFile

@Document("Researches")
data class Research(
	@Id val id: ObjectId = ObjectId(),
	@Field val researchCode: String = "",
	@Field val creatorLogin: String = "",
	@Field val title: String = "",
	@Field val description: String = "",
	@Field val poster: Binary = Binary(ByteArray(0)),
	@Field val participantLimit: Int = 0,
	@Field val participants: List<String> = listOf(),
	@Field val begDate: String = "",
	@Field val endDate: String = "",
	@Field val location: ResearchLocation = ResearchLocation("", ""),
	@Field val rewards: List<ResearchReward> = listOf(),
	@Field val requirements: List<ResearchRequirement> = listOf()
)

class ResearchUpdateRequest(
	val title: String?,
	val description: String?,
	val participantLimit: Int?,
	val location: ResearchLocation?
)

class ResearchRequest(
	private val title: String,
	private val description: String,
	private val creatorLogin: String,
	private val begDate: String,
	private val endDate: String,
	private val participantLimit: Int,
	private val location: ResearchLocation,
	private val rewards: List<ResearchReward>,
	private val requirements: List<ResearchRequirement>
) {
	fun toResearch(posterFile: MultipartFile): Research {
		return Research(
			title = this.title,
			description = this.description,
			creatorLogin = this.creatorLogin,
			poster = Binary(BsonBinarySubType.BINARY, posterFile.bytes),
			begDate = this.begDate,
			endDate = this.endDate,
			participantLimit = this.participantLimit,
			location = this.location,

			rewards = this.rewards.map { reward ->
				when (reward.type) {
					"cash" -> ResearchReward(reward.type, reward.value.toString().toInt())
					"item" -> ResearchReward(reward.type, reward.value as String)
					else -> ResearchReward("", "")
				}
			},

			requirements = this.requirements.map { req ->
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
		)
	}
}

class ResearchLocation(
	private val form: String,
	private val place: String
)

class ResearchReward(
	val type: String,
	val value: Any
)

class ResearchRequirement(
	val type: String,
	val criteria: Any
)

class ResearchRequirementAgeInterval(
	private val ageMin: Int,
	private val ageMax: Int
)

class ResearchRequirementOther(
	private val type: String,
	private val description: String
)