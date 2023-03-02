package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserRepositoryTests {

    @Autowired lateinit var userService: UserService
    @Autowired lateinit var userRepository: UserRepository


    @Test
    fun `add new User by userRepository`() {
        // GIVEN
        val userTestObject = User(
            "_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG",
            true
        )
        val testUserID = userTestObject.id

        // WHEN
        userRepository.save(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userService.getUserById(testUserID).get(),
            "Users are not the same (addUser failed)."
        )
    }

    @Test
    fun `delete existing User by userRepository`() {
        // GIVEN
        val userTestObject = User(
            "_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG",
            true
        )
        val testUserID = userTestObject.id
        userService.addUser(userTestObject)

        // WHEN
        userRepository.deleteById(testUserID)

        // THEN
        assertTrue(userService.getUserById(testUserID).isEmpty, "User has not been deleted (deleteUser failed).")
    }

    @Test
    fun `update existing User data by userRepository`() {
        // GIVEN

        val userTestObject = User(
            "_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG",
            true)

        val testUserID = userTestObject.id

        val newUserPhoneNumber: String = "987654321"
        val newUserGender: String = "Female"

        userService.addUser(userTestObject)

        // WHEN

        userTestObject.phone = newUserPhoneNumber
        userTestObject.gender = newUserGender

        userRepository.save(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userService.getUserById(testUserID).get(),
            "User has not been changed (update failed)."
        )
    }


    @BeforeEach
    fun beforeEach() {
        val testUserID = "_testID"
        userService.deleteUserById(testUserID)
    }


}

