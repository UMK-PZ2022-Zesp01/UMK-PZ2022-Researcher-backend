package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

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
	@Field val avatarImage: String = "",
	@Field val location: String = "",
	@Field val isConfirmed: Boolean = false
) {

	fun toUserResponse(): UserResponse {
		return UserResponse(
			login = login,
			firstName = firstName,
			lastName = lastName,
			email = email,
			phone = phone,
			location = location,
			birthDate = birthDate,
			gender = gender,
			avatarImage = avatarImage,
		)
	}
}

class UserRegisterRequest(
	val firstName: String,
	val lastName: String,
	val login: String,
	val email: String,
	val password: String,
	val gender: String,
	val birthDate: String,
) {
	fun toUser(): User {
		return User(
			login = login,
			password = BCrypt.hashpw(password, BCrypt.gensalt()),
			firstName = firstName,
			lastName = lastName,
			email = email,
			gender = gender,
			birthDate = birthDate
		)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as UserRegisterRequest

		if (firstName != other.firstName) return false
		if (lastName != other.lastName) return false
		if (login != other.login) return false
		if (email != other.email) return false
		if (password != other.password) return false
		if (gender != other.gender) return false
		return birthDate == other.birthDate
	}

	// Delete if it causes problems
	override fun hashCode(): Int {
		var result = firstName.hashCode()
		result = 31 * result + lastName.hashCode()
		result = 31 * result + login.hashCode()
		result = 31 * result + email.hashCode()
		result = 31 * result + password.hashCode()
		result = 31 * result + gender.hashCode()
		result = 31 * result + birthDate.hashCode()
		return result
	}
}

class UserUpdateRequest(
	val password: String?,
	val firstName: String?,
	val lastName: String?,
	val email: String?,
	val phone: String?,
	val location: String?
)

class UserResponse(
	private val login: String,
	private val firstName: String,
	private val lastName: String,
	private val email: String,
	private val phone: String,
	private val location: String,
	private val birthDate: String,
	private val gender: String,
	private val avatarImage: String
)

class LoginData(
	val login: String,
	val password: String
)