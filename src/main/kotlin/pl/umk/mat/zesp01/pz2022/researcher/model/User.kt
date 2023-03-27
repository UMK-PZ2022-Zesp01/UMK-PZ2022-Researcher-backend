package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
data class User(
    @Id var id: String = "",
    @Field var login: String = "",
    @Field var password: String = "",
    @Field var firstName: String = "",
    @Field var lastName: String = "",
    @Field var email: String = "",
    @Field var phone: String = "",
    @Field var birthDate: String = "",
    @Field var gender: String = "",
    @Field var avatarImage: String = "",
    @Field var location: String = "",
    @Field var isConfirmed: Boolean = false
) {

    fun toUserProfileDTO(): UserProfileDTO {
        val userProfileDTO = UserProfileDTO()

        userProfileDTO.id = this.id
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
    var id: String = "",
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

class UserRegisterData(
    var firstName: String = "",
    var lastName: String = "",
    var login: String = "",
    var email: String = "",
    var password: String="",
    var gender: String = "",
    var birthDate: String = ""
){
    fun toUser(): User{
        val user = User()

        user.firstName=firstName
        user.lastName=lastName
        user.login=login
        user.email=email
        user.password=password
        user.gender=gender
        user.birthDate=birthDate

        return user;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserRegisterData

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (login != other.login) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (gender != other.gender) return false
        if (birthDate != other.birthDate) return false

        return true
    }



}



