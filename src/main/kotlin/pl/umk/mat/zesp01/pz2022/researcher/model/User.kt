package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
class User(
    @Id var id: String = "",
    @Field var login: String = "",
    @Field var password: String = "",
    @Field var firstName: String = "",
    @Field var lastName: String = "",
    @Field var email: String = "",
    @Field var phone: String = "",
    @Field var birthDate: String = "",
    @Field var gender: String = "",
    @Field var avatarImage: String = ""
) {

    fun toUserProfileDTO(): UserProfileDTO {
        val userProfileDTO = UserProfileDTO()

        userProfileDTO.login = this.login
        userProfileDTO.firstName = this.firstName
        userProfileDTO.lastName = this.lastName
        userProfileDTO.email = this.email
        userProfileDTO.phone = this.phone
        userProfileDTO.birthDate = this.birthDate
        userProfileDTO.gender = this.gender
        userProfileDTO.avatarImage = this.avatarImage

        return userProfileDTO
    }
}

class UserProfileDTO(
    var login: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phone: String = "",
    var birthDate: String = "",
    var gender: String = "",
    var avatarImage: String = ""
)

class LoginData(
    var login: String = "",
    var password: String = ""
)


