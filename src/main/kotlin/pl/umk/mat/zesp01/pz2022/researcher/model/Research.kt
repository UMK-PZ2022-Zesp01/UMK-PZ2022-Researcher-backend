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
	@Field var researchCode: String = "",
	@Field var creatorLogin: String = "",
	@Field var title: String = "",
	@Field var description: String = "",
	@Field var poster: Binary = Binary(ByteArray(0)),
	@Field var participantLimit: Int = 0,
	@Field var participants: List<String> = listOf(),
	@Field var begDate: String = "",
	@Field var endDate: String = "",
	@Field var location: ResearchLocation = ResearchLocation("", ""),
	@Field var rewards: List<ResearchReward> = listOf(),
	@Field var requirements: List<ResearchRequirement> = listOf()
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Research

        if (researchCode != other.researchCode) return false
        if (creatorLogin != other.creatorLogin) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (poster != other.poster) return false
        if (participantLimit != other.participantLimit) return false
        if (participants != other.participants) return false
        if (begDate != other.begDate) return false
        if (endDate != other.endDate) return false
        if (location != other.location) return false
        if (rewards != other.rewards) return false
        return requirements == other.requirements
    }

    override fun hashCode(): Int {
        var result = researchCode.hashCode()
        result = 31 * result + creatorLogin.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + poster.hashCode()
        result = 31 * result + participantLimit
        result = 31 * result + participants.hashCode()
        result = 31 * result + begDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + rewards.hashCode()
        result = 31 * result + requirements.hashCode()
        return result
    }

}

data class ResearchUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val participantLimit: Int? = null,
    val location: ResearchLocation? = null
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
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResearchResponse

        if (researchCode != other.researchCode) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (poster != other.poster) return false
        if (creatorLogin != other.creatorLogin) return false
        if (begDate != other.begDate) return false
        if (endDate != other.endDate) return false
        if (participants != other.participants) return false
        if (participantLimit != other.participantLimit) return false
        if (location != other.location) return false
        if (rewards != other.rewards) return false
        return requirements == other.requirements
    }

    override fun hashCode(): Int {
        var result = researchCode.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + poster.hashCode()
        result = 31 * result + creatorLogin.hashCode()
        result = 31 * result + begDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + participants
        result = 31 * result + participantLimit
        result = 31 * result + location.hashCode()
        result = 31 * result + rewards.hashCode()
        result = 31 * result + requirements.hashCode()
        return result
    }
}

class ResearchLocation(
    private val form: String,
    private val place: String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResearchLocation

        if (form != other.form) return false
        return place == other.place
    }

    override fun hashCode(): Int {
        var result = form.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}

class ResearchReward(
    val type: String,
    val value: Any
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResearchReward

        if (type != other.type) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class ResearchRequirement(
    val type: String,
    val criteria: Any
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResearchRequirement

        if (type != other.type) return false
        return criteria == other.criteria
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + criteria.hashCode()
        return result
    }
}

class ResearchRequirementAgeInterval(
    @JsonProperty("ageMin") private val ageMin: Int,
    @JsonProperty("ageMax") private val ageMax: Int
)

class ResearchRequirementOther(
    @JsonProperty("type") private val type: String,
    @JsonProperty("description") private val description: String
)
