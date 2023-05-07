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
	@Field val isConfirmed: Boolean = false,
	@Field val lastLoggedIn: Boolean = false,
) {

	fun toUserResponse(): UserResponse {
		return UserResponse(
			login = this.login,
			firstName = this.firstName,
			lastName = this.lastName,
			email = this.email,
			phone = this.phone,
			location = this.location,
			birthDate = this.birthDate,
			gender = this.gender,
			avatarImage = Base64.getEncoder().encodeToString(this.avatarImage.data),
			lastLoggedIn = this.lastLoggedIn
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
}

data class UserUpdateRequest(
	val password: String? = null,
	val firstName: String? = null,
	val lastName: String? = null,
	val email: String? = null,
	val phone: String? = null,
	val location: String? = null,
	var lastLoggedIn: Boolean? = null
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
	private val avatarImage: String,
	private val lastLoggedIn: Boolean
)

class LoginData(
	val login: String,
	val password: String,
	val rememberDevice: Boolean,
)