package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserRepositoryTests {

    @Autowired lateinit var userService: UserService
    @Autowired lateinit var userRepository: UserRepository
    lateinit var userTestObject: User
    lateinit var testUserLogin: String


    @BeforeEach
    fun setup() {
        userTestObject = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false
        )
        testUserLogin = userTestObject.login
        userService.deleteUserByLogin(testUserLogin)
    }

    @Test
    fun `add new User by userRepository`() {
        // GIVEN (userTestObject)

        // WHEN
        userRepository.save(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userService.getUserByLogin(testUserLogin).get(),
            "Users are not the same (addUser failed)."
        )
    }

    @Test
    fun `delete existing User by userRepository`() {
        // GIVEN (userTestObject)
        userService.addUser(userTestObject)

        // WHEN
        userRepository.deleteByLogin(testUserLogin)

        // THEN
        assertTrue(userService.getUserByLogin(testUserLogin).isEmpty, "User has not been deleted (deleteUser failed).")
    }

    @Test
    fun `update existing User data by userRepository`() {
        // GIVEN (userTestObject)
        val newUserPhoneNumber = "987654321"
        val newUserGender = "Female"

        userService.addUser(userTestObject)

        // WHEN
        userTestObject.phone = newUserPhoneNumber
        userTestObject.gender = newUserGender

        userRepository.save(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userService.getUserByLogin(testUserLogin).get(),
            "User has not been changed (update failed)."
        )
    }

    @Test
    fun `get user by email using userRepository`() {
        // GIVEN
        userService.addUser(userTestObject)
        val testUserMail = userTestObject.email

        // WHEN
        val result = userRepository.findUserByEmail(testUserMail)

        // THEN
        assertEquals(Optional.of(userTestObject), result)
    }

    @Test
    fun `get user by login using userRepository`() {
        // GIVEN
        userService.addUser(userTestObject)
        val testUserLogin = userTestObject.login

        // WHEN
        val result = userRepository.findUserByLogin(testUserLogin)

        // THEN
        assertEquals(Optional.of(userTestObject), result)
    }

    @Test
    fun `get users by firstname using userRepository`() {
        // GIVEN
        userService.addUser(userTestObject)
        val testUserFirstName = userTestObject.firstName

        // WHEN
        val result = userRepository.findUserByFirstName(testUserFirstName)

        // THEN
        assertEquals(Optional.of(listOf(userTestObject)), result)
    }

    @Test
    fun `get users by lastname using userRepository`() {
        // GIVEN
        userService.addUser(userTestObject)
        val testUserLastName = userTestObject.lastName

        // WHEN
        val result = userRepository.findUserByLastName(testUserLastName)

        // THEN
        assertEquals(Optional.of(listOf(userTestObject)), result)
    }
}

