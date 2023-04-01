package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
data class User(
	@Id val id: ObjectId = ObjectId(),
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
		if (birthDate != other.birthDate) return false

		return true
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