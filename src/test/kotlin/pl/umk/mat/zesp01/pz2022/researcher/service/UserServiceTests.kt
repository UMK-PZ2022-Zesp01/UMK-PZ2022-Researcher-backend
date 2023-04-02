package pl.umk.mat.zesp01.pz2022.researcher.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserServiceTests {

    @Autowired lateinit var userService: UserService
    @Autowired lateinit var userRepository: UserRepository
    lateinit var userTestObject: User
    lateinit var testUserID: String


    @Test
    fun `add new User by UserService`() {
        // GIVEN (userTestObject)

        // WHEN
        userService.addUser(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userRepository.findById(testUserID).get(),
            "Users are not the same (addUser failed)."
        )
    }

//    @Test
//    fun `delete existing user by UserService`() {
//        // GIVEN
//
//        val testUserID = userTestObject.id
//        userRepository.save(userTestObject)
//
//        // WHEN
//        userService.deleteUserById(testUserID)
//
//        // THEN
//        assertTrue(userRepository.findById(testUserID).isEmpty, "User has not been deleted (deleteUser failed).")
//    }


//    @Test
//    fun `update existing User data by userService`() {
//        // GIVEN
//        val newUserPhoneNumber = "987654321"
//        val newUserGender = "Female"
//
//        userRepository.save(userTestObject)
//
//        // WHEN
//        userTestObject.phone = newUserPhoneNumber
//        userTestObject.gender = newUserGender
//
//        userService.updateUserById(testUserID, userTestObject)
//
//        // THEN
//        assertTrue(
//            userTestObject == userRepository.findById(testUserID).get(),
//            "User has not been changed (update failed)."
//        )
//    }

//    @Test
//    fun `get all user IDs using userService`() {
//        // GIVEN
//        val userTestObject2 = User(
//            "_testID2",
//            "_testLOGIN2",
//            "testPASSWORD2",
//            "testFIRSTNAME2",
//            "testLASTNAME2",
//            "testEMAIL@test.com2",
//            "1234567892",
//            "02-01-1970",
//            "Female",
//            "testAVATARIMAGE2.IMG",
//            false)
//
//         userRepository.saveAll(listOf(userTestObject, userTestObject2))
//
//        // WHEN
//        val result = userService.getAllUserIds()
//
//        // THEN
//        assertEquals(listOf("{\"_id\": \"_testID\"}", "{\"_id\": \"_testID2\"}"), result)
//    }

//    @Test
//    fun `get user by ID using userService`() {
//        // GIVEN
//        userRepository.save(userTestObject)
//
//        // WHEN
//        val result = userService.getUserById(testUserID)
//
//        // THEN
//        assertEquals(Optional.of(userTestObject), result)
//    }

//    @Test
//    fun `get user by email using userService`() {
//        // GIVEN
//        userRepository.save(userTestObject)
//        val testUserMail = userTestObject.email
//
//        // WHEN
//        val result = userService.getUserByEmail(testUserMail)
//
//        // THEN
//        assertEquals(Optional.of(userTestObject), result)
//    }

    @Test
    fun `get user by login using userService`() {
        // GIVEN
        userRepository.save(userTestObject)
        val testUserLogin = userTestObject.login

        // WHEN
        val result = userService.getUserByLogin(testUserLogin)

        // THEN
        assertEquals(Optional.of(userTestObject), result)
    }

//    @Test
//    fun `get users by firstname using userService`() {
//        // GIVEN
//        userRepository.save(userTestObject)
//        val testUserFirstName = userTestObject.firstName
//
//        // WHEN
//        val result = userService.getUsersByFirstName(testUserFirstName)
//
//        // THEN
//        assertEquals(listOf(userTestObject), result)
//    }

//    @Test
//    fun `get users by lastname using userService`() {
//        // GIVEN
//        userRepository.save(userTestObject)
//        val testUserLastName = userTestObject.lastName
//
//        // WHEN
//        val result = userService.getUsersByLastName(testUserLastName)
//
//        // THEN
//        assertEquals(listOf(userTestObject), result)
//    }

//    @Test
//    fun `get users by gender using userService`() {
//        // GIVEN
//        val userTestObject2 = userTestObject
//        userTestObject2.gender = "Female"
//        userRepository.saveAll(listOf(userTestObject, userTestObject2))
//
//        // WHEN
//        val result = userService.findUsersByGender("Female")
//
//        // THEN
//        assertEquals(1, result.size)
//        assertEquals(userTestObject2, result[0])
//    }

//    @BeforeEach
//    fun setup() {
//        userRepository.deleteAll()
//        userTestObject = User(
//            "_testID",
//            "_testLOGIN",
//            "testPASSWORD",
//            "testFIRSTNAME",
//            "testLASTNAME",
//            "testEMAIL@test.com",
//            "123456789",
//            "01-01-1970",
//            "Male",
//            "testAVATARIMAGE.IMG",
//            false)
//        testUserID = userTestObject.id;
//    }

}

