package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
data class User(
	@Id val id: ObjectId = ObjectId(),
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

}

class UserUpdateRequest(
	val login: String = "",
	val password: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val email: String = "",
	val phone: String = "",
	val birthDate: String = "",
	val gender: String = "",
	val avatarImage: String = ""
)

class UserResponse(
	val login: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val email: String = "",
	val phone: String = "",
	val birthDate: String = "",
	val gender: String = "",
	val avatarImage: String = ""
)

class LoginData(
	val login: String,
	val password: String,
)