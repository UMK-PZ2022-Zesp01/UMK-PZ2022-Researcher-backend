package pl.umk.mat.zesp01.pz2022.researcher.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*
import kotlin.math.log

@Service
class UserService(
	@Autowired val userRepository: UserRepository,
	@Autowired val researchRepository: ResearchRepository,
	@Autowired val researchService: ResearchService,
	@Autowired val mongoOperations: MongoOperations,
) {

	fun addUser(user: User): User =
		userRepository.insert(user)

	fun updateUser(user: User, userData: UserUpdateRequest): String {
		if (userData.email != null) {
			if (isEmailAlreadyTaken(userData.email)) return "email"
		}
		if (userData.phone != null) {
			if (isPhoneAlreadyTaken(userData.phone)) return "phone"
		}
		val updatedUser = user.copy(
			password = if (userData.password != null) BCrypt.hashpw(userData.password, BCrypt.gensalt())
			else user.password,
			firstName = userData.firstName ?: user.firstName,
			lastName = userData.lastName ?: user.lastName,
			email = userData.email ?: user.email,
			phone = userData.phone ?: user.phone,
			location = userData.location ?: user.location,
			locationCoords=userData.locationCoords?:user.locationCoords,
			lastLoggedIn = userData.lastLoggedIn ?: user.lastLoggedIn
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("login").`is`(user.login)),
			updatedUser
		)
		return "ok"
	}

	fun updateUserPassword(user: User, userData: UserPasswordUpdateRequest): String {
		if (!BCrypt.checkpw(userData.password, user.password)) {
			return	"diff"
		}
		val updatedUser = user.copy(
				password = if (userData.newPassword != null) BCrypt.hashpw(userData.newPassword, BCrypt.gensalt())
				else user.password,
				firstName = userData.firstName ?: user.firstName,
				lastName = userData.lastName ?: user.lastName,
				email = userData.email ?: user.email,
				phone = userData.phone ?: user.phone,
				location = userData.location ?: user.location,
				locationCoords=userData.locationCoords?:user.locationCoords,
				lastLoggedIn = userData.lastLoggedIn ?: user.lastLoggedIn
		)
		mongoOperations.findAndReplace(
				Query.query(Criteria.where("login").`is`(user.login)),
				updatedUser
		)
		return "ok"
	}

	fun overrideUserPassword(user: User, newPassword:String):String{
		val updatedUser = user.copy(
			password = BCrypt.hashpw(newPassword, BCrypt.gensalt())
		)

		mongoOperations.findAndReplace(
			Query.query(Criteria.where("login").`is`(user.login)),
			updatedUser
		)
		return "ok"
	}


	fun updateUserAvatar(user:User,avatar:MultipartFile){
		val updatedUser=user.copy(
			avatarImage=Binary(BsonBinarySubType.BINARY, avatar.bytes)
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

	fun getUserByLogin(login: String): Optional<User> =
		userRepository.findUserByLogin(login)

	fun deleteUserByLogin(login: String){
		userRepository.deleteUserByLogin(login)
		researchRepository.deleteAllByCreatorLogin(login)
		researchService.removeUserFromAllResearches(login)

	}


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

	fun isGoogleAccount(email: String): Boolean {
		val user = userRepository.findUserByEmail(email)
		return user.get().isGoogle
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
			result.add(temporaryPhone)
		}
		return result
	}

	fun isPhoneAlreadyTaken(phone: String): Boolean {
		val phoneList = getAllUserPhones()
		return phoneList.contains(phone)
	}

	fun deleteCheck(user: User, deleteRequest: DeleteRequest):String {
		if (!BCrypt.checkpw(deleteRequest.password, user.password)) {
			return	"diff"
		}
		return "ok"
	}

	fun deleteGoogleCheck(user: User, deleteGoogleRequest: DeleteGoogleRequest):String {
		if (deleteGoogleRequest.email==null)
			throw Exception()
		val transport = NetHttpTransport()
		val jsonFactory: JsonFactory = GsonFactory()
		val verifier = GoogleIdTokenVerifier.Builder(
				transport,
				jsonFactory
		)
				.setAudience(listOf(System.getenv("CLIENT_ID")))
				.build()
		val idToken: GoogleIdToken = verifier.verify(deleteGoogleRequest.jwt) ?: throw Exception()
		val payload: GoogleIdToken.Payload = idToken.payload
		if(deleteGoogleRequest.email==payload.email){
			return "ok"
		}
		return "diff"
	}
}