package pl.umk.mat.zesp01.pz2022.researcher.model

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
//    @Field var requirement = listOf<ResearchRequirement>()
}

class ResearchResponse(
    var id: String,
    var researchCreatorId: String
    // TODO
)

class ResearchRequest(
    val title: String,
    val description: String,
    val creatorLogin: String,
    val posterId: String,
    val begDate: String,
    val endDate: String,
    val participantLimit: Int,
    val location: ResearchLocation,
    val rewards: List<ResearchReward>
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
        research.rewards = this.rewards

        return research
    }
}

class ResearchLocation(
    var form: String = "",
    var place: String = ""
)

class ResearchReward(
    var type: String = "",
    var value: String = ""
)

class ResearchRequirement(
    var type: String = "",
    var ageMin: Int = -1,
    var ageMax: Int = -1,
    var genderNames: String = "",
    var custom: String = ""
)