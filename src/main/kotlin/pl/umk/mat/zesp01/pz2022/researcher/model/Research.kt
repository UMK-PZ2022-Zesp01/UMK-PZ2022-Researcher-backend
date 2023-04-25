package pl.umk.mat.zesp01.pz2022.researcher.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.web.multipart.MultipartFile
import java.awt.im.InputMethodHighlight
import java.time.LocalDate
import java.util.*

@Document("Researches")
data class Research(
	val researchCode: String = "",
	@Indexed(name = "creatorLoginIndex") val creatorLogin: String = "",
	val creatorFullName: String = "",
	val creatorEmail: String = "",
	val creatorPhone: String = "",
	val title: String = "",
	val description: String = "",
	val poster: Binary = Binary(ByteArray(0)),
	val participantLimit: Int = 0,
	val participants: List<String> = listOf(),
	val begDate: String = "",
	val endDate: String = "",
	val location: ResearchLocation = ResearchLocation("", ""),
	val rewards: List<ResearchReward> = listOf(),
	val requirements: List<ResearchRequirement> = listOf()
) {
	fun toResearchResponse(): ResearchResponse =
		ResearchResponse(
			researchCode = researchCode,
			title = title,
			description = description,
//			poster = Base64.getEncoder().encodeToString(poster.data),
			creatorLogin = creatorLogin,
			creatorFullName = creatorFullName,
			creatorEmail = creatorEmail,
			creatorPhone = creatorPhone,
			begDate = begDate,
			endDate = endDate,
			currentDate = LocalDate.now().toString(),
			participants = participants.size,
			participantLimit = participantLimit,
			location = location,
			rewards = rewards,
			requirements = requirements
		)
}

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
	private val creatorFullName: String,
	private val creatorEmail: String,
	private val creatorPhone: String,
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
			creatorFullName = this.creatorFullName,
			creatorEmail = this.creatorEmail,
			creatorPhone = this.creatorPhone,
			poster = Binary(BsonBinarySubType.BINARY, posterFile.bytes),
			begDate = this.begDate,
			endDate = this.endDate,
			participantLimit = this.participantLimit,
			location = this.location,

			rewards = this.rewards.map { reward ->
				when (reward.type) {
					"cash" -> ResearchReward(reward.type, reward.value.toString().toInt())
					"item" -> ResearchReward(reward.type, reward.value as String)
					"other" -> ResearchReward(reward.type, reward.value as String)
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

class ResearchResponse(
	private val researchCode: String,
	private val title: String,
	private val description: String,
//	private val poster: String,
	private val creatorLogin: String,
	private val creatorFullName: String,
	private val creatorEmail: String,
	private val creatorPhone: String,
	private val begDate: String,
	private val endDate: String,
	private val currentDate: String,
	private val participants: Int,
	private val participantLimit: Int,
	private val location: ResearchLocation,
	private val rewards: List<ResearchReward>,
	private val requirements: List<ResearchRequirement>
)

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
	@JsonProperty("ageMin") private val ageMin: Int,
	@JsonProperty("ageMax") private val ageMax: Int
)

class ResearchRequirementOther(
	@JsonProperty("type") private val type: String,
	@JsonProperty("description") private val description: String
)


class ResearchFilters(
	val age: Int? = null,
	val gender: String? = null,
	val form: List<String>? = null,
	val minDate: String? = null,
	val maxDate: String? = null,
	val availableOnly: Boolean = false,
//	val city: String? = null,

)