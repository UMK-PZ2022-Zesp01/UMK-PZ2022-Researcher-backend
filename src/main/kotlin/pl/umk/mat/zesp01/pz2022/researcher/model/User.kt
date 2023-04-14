package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
data class User(
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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as User

		if (login != other.login) return false
		if (password != other.password) return false
		if (firstName != other.firstName) return false
		if (lastName != other.lastName) return false
		if (email != other.email) return false
		if (phone != other.phone) return false
		if (birthDate != other.birthDate) return false
		if (gender != other.gender) return false
		if (location != other.location) return false
		return isConfirmed == other.isConfirmed
	}

	override fun hashCode(): Int {
		var result = login.hashCode()
		result = 31 * result + password.hashCode()
		result = 31 * result + firstName.hashCode()
		result = 31 * result + lastName.hashCode()
		result = 31 * result + email.hashCode()
		result = 31 * result + phone.hashCode()
		result = 31 * result + birthDate.hashCode()
		result = 31 * result + gender.hashCode()
		result = 31 * result + avatarImage.hashCode()
		result = 31 * result + location.hashCode()
		result = 31 * result + isConfirmed.hashCode()
		return result
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

data class UserUpdateRequest(
	val password: String? = null,
	val firstName: String? = null,
	val lastName: String? = null,
	val email: String? = null,
	val phone: String? = null,
	val location: String? = null
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
	val password: String,
	val rememberDevice: Boolean,
)