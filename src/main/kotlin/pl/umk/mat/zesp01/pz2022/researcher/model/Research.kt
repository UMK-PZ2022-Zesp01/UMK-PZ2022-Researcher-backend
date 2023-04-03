package pl.umk.mat.zesp01.pz2022.researcher.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

@Document("Researches")
data class Research(
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
) {
    fun toResearchResponse(): ResearchResponse {
        return ResearchResponse(
            researchCode = researchCode,
            title = title,
            description = description,
            poster = Base64.getEncoder().encodeToString(poster.data),
            creatorLogin = creatorLogin,
            begDate = begDate,
            endDate = endDate,
            participants = participants.size,
            participantLimit = participantLimit,
            location = location,
            rewards = rewards,
            requirements = requirements
        )
    }
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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchRequest

		if (title != other.title) return false
		if (description != other.description) return false
		if (creatorLogin != other.creatorLogin) return false
		if (begDate != other.begDate) return false
		if (endDate != other.endDate) return false
		if (participantLimit != other.participantLimit) return false
		if (location != other.location) return false
		if (rewards != other.rewards) return false
		if (requirements != other.requirements) return false

		return true
	}
}

class ResearchResponse(
    private val researchCode: String,
    private val title: String,
    private val description: String,
    private val poster: String,
    private val creatorLogin: String,
    private val begDate: String,
    private val endDate: String,
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
