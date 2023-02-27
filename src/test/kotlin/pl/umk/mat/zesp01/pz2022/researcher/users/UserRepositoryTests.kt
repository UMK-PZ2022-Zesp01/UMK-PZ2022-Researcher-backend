package pl.umk.mat.zesp01.pz2022.researcher.users

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService

@SpringBootTest
@ActiveProfiles("integration")
class UserRepositoryTests {

    @Autowired lateinit var userService: UserService
    @Autowired lateinit var userRepository: UserRepository


    @Test
    fun `add new User`() {
        // GIVEN (SampleUser.userTestObject)
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
    fun `delete existing user`() {
        // GIVEN (SampleUser.userTestObject)
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
        userService.deleteUserById(testUserID)
        // or maybe userRepository.deleteById(testUserID)

        // THEN
        assertTrue(userService.getUserById(testUserID).isEmpty, "User has not been deleted (deleteUser failed).")
    }

    @Test
    fun `update user data by userRepository`() {
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

        // GIVEN
        val newUserPhoneNumber: String = "987654321"
        val newUserGender: String = "Female"

        // WHEN
        userRepository.save(userTestObject)

        userTestObject.phone = newUserPhoneNumber
        userTestObject.gender = newUserGender

        userRepository.save(userTestObject)
        // THEN
        assertTrue(
            userTestObject == userRepository.findUserByEmail(userTestObject.email).get(),
            "User has not been changed (update failed)."
        )
    }


    @BeforeEach
    fun beforeEach() {
        val testUserID = "_testID"
        userService.deleteUserById(testUserID)
    }


}

