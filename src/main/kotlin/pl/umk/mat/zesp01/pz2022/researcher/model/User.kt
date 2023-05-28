package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.Binary
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

@Document("Users")
data class User(
	@Field val login: String = "",
	@Field val password: String = "",
	@Field val firstName: String = "",
	@Field val lastName: String = "",
	@Field val email: String = "",
	@Field val phone: String = "",
	@Field val birthDate: String = "",
	@Field val gender: String = "",
	@Field val avatarImage: Binary = Binary(ByteArray(0)),
	@Field val location: String = "",
	@Field val locationCoords:List<String> = listOf(),
	@Field val isConfirmed: Boolean = false,
	@Field val lastLoggedIn: Boolean = false,
	@Field val isGoogle: Boolean = false,
) {

	fun toSafeUserResponse(): SafeUserResponse{
		return SafeUserResponse(
			login = login,
			firstName = firstName,
			lastName = lastName,
			location = location,
			locationCoords=locationCoords,
			gender = gender,
			avatarImage = Base64.getEncoder().encodeToString(avatarImage.data),
		)
	}

	fun toUserResponse(): UserResponse {
		return UserResponse(
			login = login,
			firstName = firstName,
			lastName = lastName,
			email = email,
			phone = phone,
			location = location,
			locationCoords=locationCoords,
			birthDate = birthDate,
			gender = gender,
			avatarImage = Base64.getEncoder().encodeToString(avatarImage.data),
			lastLoggedIn = lastLoggedIn,
			isGoogle = isGoogle
		)
	}

    fun toParticipantsData(): ParticipantsData =
        ParticipantsData(
            login = this.login,
            fullName = "${this.firstName} ${this.lastName}",
            email = this.email,
            location = this.location
        )
}

class UserRegisterRequest(
    val firstName: String,
    val lastName: String,
    val login: String,
    val email: String,
    val password: String,
    val gender: String,
    val birthDate: String,
	val isGoogle:Boolean,
) {
    fun toUser(): User {
        return User(
            login = login,
            password = BCrypt.hashpw(password, BCrypt.gensalt()),
            firstName = firstName,
            lastName = lastName,
            email = email,
            gender = gender,
            birthDate = birthDate,
			isGoogle = isGoogle
        )
    }
}

data class UserUpdateRequest(
	val password: String? = null,
	val firstName: String? = null,
	val lastName: String? = null,
	val email: String? = null,
	val phone: String? = null,
	val location: String? = null,
	val locationCoords: List<String>?=null,
	val lastLoggedIn: Boolean? = null,
	val isGoogle: Boolean?=null
)

data class GoogleLoginRequest(
	val email:String?=null,
	val jwt:String?=null,
)

data class UserPasswordUpdateRequest(
	val password: String? = null,
	val newPassword: String? = null,
	val firstName: String? = null,
	val lastName: String? = null,
	val email: String? = null,
	val phone: String? = null,
	val location: String? = null,
	val locationCoords: List<String>?=null,
	val lastLoggedIn: Boolean? = null,
	val isGoogle: Boolean?=null
)

data class DeleteRequest(
    val password: String? = null,
)

data class UserResponse(
	private val login: String,
	private val firstName: String,
	private val lastName: String,
	private val email: String,
	private val phone: String,
	private val location: String,
	private val locationCoords: List<String>,
	private val birthDate: String,
	private val gender: String,
	private val avatarImage: String,
	private val lastLoggedIn: Boolean,
	private val isGoogle: Boolean
)

data class SafeUserResponse(
	private val login: String,
	private val firstName: String,
	private val lastName: String,
	private val location: String,
	private val locationCoords: List<String>,
	private val gender: String,
	private val avatarImage: String,
)

class LoginData(
    val login: String,
    val password: String,
    val rememberDevice: Boolean,
)

class ParticipantsData(
    val login: String = "",
    val fullName: String = "",
    val email: String = "",
    val location: String = ""
)

class PasswordResetRequest(
    val token: String = "",
    val newPassword: String = "",
)