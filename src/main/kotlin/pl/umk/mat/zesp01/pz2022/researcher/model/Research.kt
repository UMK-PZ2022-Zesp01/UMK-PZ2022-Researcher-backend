package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In
import org.springframework.data.mongodb.core.mapping.Document
import java.net.URL

@Document("Researches")
class Research {
    @Id
    var id: String = ""
    var userId: String = ""
    var participants = listOf<User>()
    var title: String = ""
    var description: String = ""
    var durationTimeInMinutes: Int = 0
    var begDate: String = ""
    var endDate: String = ""
    var isActive: Boolean = false
    var locationForm: String = ""
    var researchPlace: String = ""
    var researchImage: String = ""
    var minAgeRequirement: Int = 0
    var maxAgeRequirement: Int = 0
    var participantLimit: Int = 0
    var genderRequirement: String = ""
    var reward = listOf<String>()

}