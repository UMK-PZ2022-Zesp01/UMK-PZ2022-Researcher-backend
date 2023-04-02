package pl.umk.mat.zesp01.pz2022.researcher.service

import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*
import kotlin.collections.ArrayList

@Service
class UserService(
	@Autowired val userRepository: UserRepository,
	@Autowired val mongoOperations: MongoOperations
) {

	fun addUser(user: User): User =
		userRepository.insert(user)

	fun updateUser(user: User, userData: UserUpdateRequest) {
		val updatedUser = user.copy(
			password = if (userData.password != null) BCrypt.hashpw(userData.password, BCrypt.gensalt())
			else user.password,
			firstName = userData.firstName ?: user.firstName,
			lastName = userData.lastName ?: user.lastName,
			email = userData.email ?: user.email,
			phone = userData.phone ?: user.phone
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("login").`is`(user.login)),
			updatedUser
		)
	}

	fun activateUserAccount(user: User) {
		val activeUser = user.copy(isConfirmed = true)
		mongoOperations.findAndReplace(
			Query.query(Criteria.where("login").`is`(user.login)),
			activeUser
		)
	}

	fun deleteUserByLogin(login: String) =
		userRepository.deleteUserByLogin(login)

//	fun getAllUserIds(): List<String> =
//		mongoOperations.aggregate(
//			Aggregation.newAggregation(
//				Aggregation.project("_id")
//			),
//			"Users", String::class.java
//		).mappedResults

	fun getAllUserLogins(): List<String> {
		val result = mutableListOf<String>()
		val loginList = mongoOperations.aggregate(
			Aggregation.newAggregation(
				Aggregation.project().andExclude("_id").andInclude("login")
			),
			"Users", String::class.java
		).mappedResults

		loginList.forEach { login ->
			result.add(login.substring(11).dropLast(2))
		}
		return result
	}

	fun isLoginAlreadyTaken(login: String): Boolean {
		val loginList = getAllUserLogins()
		return loginList.contains(login)
	}

	fun getAllUserEmails(): List<String> {
		val result = ArrayList<String>()
		val emailList = mongoOperations.aggregate(
			Aggregation.newAggregation(
				Aggregation.project().andExclude("_id").andInclude("email")
			),
			"Users", String::class.java
		).mappedResults
		for (i in emailList) {
			val temporaryEmail = i.substring(11).dropLast(2)
			result.add(temporaryEmail)
		}
		return result
	}

	fun isEmailAlreadyTaken(email: String): Boolean {
		val emailList = getAllUserEmails()
		return emailList.contains(email)
	}

	fun getAllUserPhones(): List<String> {
		val result = ArrayList<String>()
		val phoneList = mongoOperations.aggregate(
			Aggregation.newAggregation(
				Aggregation.project().andExclude("_id").andInclude("phone")
			),
			"Users", String::class.java
		).mappedResults
		for (i in phoneList) {
			val temporaryPhone = i.substring(11).dropLast(2)
			println(temporaryPhone)
			result.add(temporaryPhone)
		}
		return result
	}

//	fun getUserById(id: String): Optional<User> =
//		userRepository.findById(id)
//
//	fun getUserByEmail(email: String): Optional<User> =
//		userRepository.findUserByEmail(email)

	fun getUserByLogin(login: String): Optional<User> =
		userRepository.findUserByLogin(login)

//	fun getUsersByFirstName(firstName: String): List<User> =
//		userRepository.findUserByFirstName(firstName)
//			.orElseThrow { throw RuntimeException("Cannot find User by First name") }
//
//	fun getUsersByLastName(lastName: String): List<User> =
//		userRepository.findUserByLastName(lastName)
//			.orElseThrow { throw RuntimeException("Cannot find User by Last name") }
//
//	// Made with MongoOperations
//	fun findUsersByGender(gender: String): List<User> =
//		mongoOperations.find(
//			Query.query(Criteria.where("gender").`is`(gender)),
//			"Users"
//		)
}