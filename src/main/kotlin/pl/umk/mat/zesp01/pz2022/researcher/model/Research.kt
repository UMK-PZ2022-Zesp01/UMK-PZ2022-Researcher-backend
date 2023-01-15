package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Researches")
class Research {
    @Id var id: String = ""
    @Field var userId: String = ""
    @Field var participants = listOf<User>()
    @Field var title: String = ""
    @Field var description: String = ""
    @Field var durationTimeInMinutes: Int = 0
    @Field var begDate: String = ""
    @Field var endDate: String = ""
    @Field var isActive: Boolean = false
    @Field var locationForm: String = ""
    @Field var researchPlace: String = ""
    @Field var researchImage: String = ""
    @Field var minAgeRequirement: Int = 0
    @Field var maxAgeRequirement: Int = 0
    @Field var participantLimit: Int = 0
    @Field var genderRequirement: String = ""
    @Field var reward = listOf<String>()
}