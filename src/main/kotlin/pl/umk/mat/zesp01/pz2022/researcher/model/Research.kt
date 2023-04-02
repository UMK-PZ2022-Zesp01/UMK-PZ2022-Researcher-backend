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
	@Field val location: ResearchLocation = ResearchLocation(),
	@Field val rewards: List<ResearchReward> = listOf(),
	@Field val requirements: List<ResearchRequirement> = listOf()
)

class ResearchResponse(
	var id: String,
	var researchCreatorId: String
	// TODO
)

class ResearchUpdateRequest(
	val title: String,
	val description: String,
	val participantLimit: Int,
	val location: ResearchLocation,
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

class ResearchLocation(
	val form: String = "",
	val place: String = ""
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchLocation

		if (form != other.form) return false
		if (place != other.place) return false

		return true
	}
}

class ResearchReward(
	val type: String = "",
	val value: Any
){
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchReward

		if (type != other.type) return false
		if (value != other.value) return false

		return true
	}

}

class ResearchRequirement(
	val type: String = "",
	val criteria: Any
){
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchRequirement

		if (type != other.type) return false
		if (criteria != other.criteria) return false

		return true
	}

}

class ResearchRequirementAgeInterval(
	val ageMin: Int = 0,
	val ageMax: Int = 0
){
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchRequirementAgeInterval

		if (ageMin != other.ageMin) return false
		if (ageMax != other.ageMax) return false

		return true
	}

}

class ResearchRequirementOther(
	val type: String = "",
	val description: String = ""
){
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ResearchRequirementOther

		if (type != other.type) return false
		if (description != other.description) return false

		return true
	}
}