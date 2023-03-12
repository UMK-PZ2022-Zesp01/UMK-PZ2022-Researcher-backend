package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Researches")
class Research {
    @Id var id: String = ""
//    @Field var creatorId: String = ""
    @Field var title: String = ""
    @Field var description: String = ""
    @Field var posterId: String = ""
    @Field var participantLimit: Int = 0
    @Field var participants = listOf<User>()
    @Field var begDate: String = ""
    @Field var endDate: String = ""
    @Field var isActive: Boolean = false
    @Field var location: ResearchLocation? = null
//    @Field var rewards: List<ResearchReward> = listOf()
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
    val posterId: String,
    val begDate: String,
    val endDate: String,
    val location: ResearchLocation
) {
    fun toResearch(): Research {
        val research = Research()

        research.title = this.title
        research.description = this.description
        research.posterId = this.posterId
        research.begDate = this.begDate
        research.endDate = this.endDate
        research.location = this.location

        return research
    }
}

class ResearchLocation(
    var form: String = "",
    var place: String = ""
)

class ResearchReward(
    var type: String = "",
    var cashValue: Int = 0,
    var itemName: String = ""
)

class ResearchRequirement(
    var type: String = "",
    var ageMin: Int = -1,
    var ageMax: Int = -1,
    var genderNames: String = "",
    var custom: String = ""
)