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
    lateinit var testUserLogin: String




    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
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
            isConfirmed = false)
        testUserLogin = userTestObject.login
    }


    @Test
    fun `add new User by UserService`() {
        // GIVEN (userTestObject)

        // WHEN
        userService.addUser(userTestObject)

        // THEN
        assertTrue(
            userTestObject == userRepository.findUserByLogin(testUserLogin).get(),
            "Users are not the same (addUser failed)."
        )
    }

    @Test
    fun `delete existing user by UserService`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        userService.deleteUserByLogin(testUserLogin)

        // THEN
        assertTrue(userRepository.findUserByLogin(testUserLogin).isEmpty, "User has not been deleted (deleteUser failed).")
    }


    @Test
    fun `update existing User data by userService`() {
        // GIVEN
        val newUserPhoneNumber = "987654321"
        val newUserGender = "Female"

        userRepository.save(userTestObject)

        // WHEN
        userTestObject.phone = newUserPhoneNumber
        userTestObject.gender = newUserGender

        userService.updateUserByLogin(testUserLogin, userTestObject)

        // THEN
        assertTrue(
            userTestObject == userRepository.findUserByLogin(testUserLogin).get(),
            "User has not been changed (update failed)."
        )
    }



    @Test
    fun `get user by email using userService`() {
        // GIVEN
        userRepository.save(userTestObject)
        val testUserMail = userTestObject.email

        // WHEN
        val result = userService.getUserByEmail(testUserMail)

        // THEN
        assertEquals(Optional.of(userTestObject), result)
    }

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

    @Test
    fun `get users by firstname using userService`() {
        // GIVEN
        userRepository.save(userTestObject)
        val testUserFirstName = userTestObject.firstName

        // WHEN
        val result = userService.getUsersByFirstName(testUserFirstName)

        // THEN
        assertEquals(listOf(userTestObject), result)
    }

    @Test
    fun `get users by lastname using userService`() {
        // GIVEN
        userRepository.save(userTestObject)
        val testUserLastName = userTestObject.lastName

        // WHEN
        val result = userService.getUsersByLastName(testUserLastName)

        // THEN
        assertEquals(listOf(userTestObject), result)
    }

    @Test
    fun `get users by gender using userService`() {
        // GIVEN
        val userTestObject2 = userTestObject
        userTestObject2.gender = "Female"
        userRepository.saveAll(listOf(userTestObject, userTestObject2))

        // WHEN
        val result = userService.findUsersByGender("Female")

        // THEN
        assertEquals(1, result.size)
        assertEquals(userTestObject2, result[0])
    }






}

