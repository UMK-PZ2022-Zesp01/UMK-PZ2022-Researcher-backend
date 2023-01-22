package pl.umk.mat.zesp01.pz2022.researcher.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import pl.umk.mat.zesp01.pz2022.researcher.users.SampleUser.*

@SpringBootTest
class UserRepositoryTests {

	@Autowired
	private lateinit var userService: UserService

	@Autowired
	private lateinit var userRepository: UserRepository

	private var testUserID = Companion.userTestObject.id


	@Test
	fun shouldAddReadAndDeleteUser() {
		// GIVEN (SampleUser.userTestObject)

		// WHEN
		userService.addUser(Companion.userTestObject)
		// or maybe "userRepository.save(SampleUser.userTestObject)"

		// THEN
		assertTrue(compareUsers(Companion.userTestObject, userService.getUserById(testUserID).get()), "Users are not the same (addUser failed).")

		// WHEN
		userService.deleteUserById(testUserID)
		// or maybe userRepository.deleteById(testUserID)

		// THEN
		assertTrue(userService.getUserById(testUserID).isEmpty, "User has not been deleted (deleteUser failed).")

	}

	@Test
	fun userDataUpdateTest(){
		// GIVEN
		val newUserPhoneNumber : String = "987654321"
		val newUserGender : String  = "Female"

		// WHEN
		userService.addUser(Companion.updatedUserTestObject)

		Companion.updatedUserTestObject.phone = newUserPhoneNumber
		Companion.updatedUserTestObject.gender = newUserGender

		userRepository.save(Companion.updatedUserTestObject)
		// THEN
		assertTrue(compareUsers(Companion.updatedUserTestObject, userService.getUserById(testUserID).get()), "User has not been changed (update failed).")
	}

	@Test
	fun userLoginTest() { // TODO
		// GIVEN
		var users : List<User> = userRepository.findAll()
		println("users defined")
		// WHEN

		// THEN
	}





	@BeforeEach
	fun beforeEach(){
		userService.deleteUserById(testUserID)
	}


	fun compareUsers(user1 : User, user2: User): Boolean{
		return (user1.id == user2.id &&
				user1.login == user2.login &&
				user1.password == user2.password &&
				user1.firstName == user2.firstName &&
				user1.lastName == user2.lastName &&
				user1.email == user2.email &&
				user1.phone == user2.phone &&
				user1.birthDate == user2.birthDate &&
				user1.gender == user2.gender &&
				user1.avatarImage == user2.avatarImage)
	}

}
