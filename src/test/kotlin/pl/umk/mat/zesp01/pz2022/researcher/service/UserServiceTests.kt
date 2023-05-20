package pl.umk.mat.zesp01.pz2022.researcher.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.model.UserPasswordUpdateRequest
import pl.umk.mat.zesp01.pz2022.researcher.model.UserUpdateRequest
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
//            avatarImage = "testAVATARIMAGE.IMG",
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
        val newUserMail = "joedoe@newmail.com"


        userRepository.save(userTestObject)

        // WHEN
        val userUpdateRequest = UserUpdateRequest(phone = newUserPhoneNumber, email = newUserMail)
        userService.updateUser(userTestObject, userUpdateRequest)

        userTestObject = userTestObject.copy(
            phone = newUserPhoneNumber,
            email = newUserMail
        )

        // THEN
        assertEquals(userTestObject, userRepository.findUserByLogin(testUserLogin).get())
    }

    @Test
    fun `get all user Logins using userService`() {
        // GIVEN
        val userTestObject2 = User(
            login = "testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL2@test.com",
            phone = "234567890",
            birthDate = "02-02-1972",
            gender = "Female",
            location = "Toruń",
            isConfirmed = false)

         userRepository.saveAll(listOf(userTestObject, userTestObject2))

        // WHEN
        val result = userService.getAllUserLogins()

        // THEN
        assertEquals(listOf(userTestObject.login, userTestObject2.login), result)
    }


    @Test
    fun `get user by login using userService`() {
        // GIVEN
        userRepository.save(userTestObject)

        // WHEN
        val result = userService.getUserByLogin(testUserLogin)

        // THEN
        assertEquals(Optional.of(userTestObject), result)
    }

    @Test
    fun `updateUserPassword returns OK when password is valid`() {

        val plainPassword = userTestObject.password
        userTestObject = userTestObject.copy(password = BCrypt.hashpw(plainPassword, BCrypt.gensalt()))
        userRepository.save(userTestObject)

        val newPassword = "newPassword123"

        val userData = UserPasswordUpdateRequest(
            plainPassword,
            newPassword,
        )

        val result = userService.updateUserPassword(userTestObject, userData)
        assertEquals("ok", result)
        assertTrue(BCrypt.checkpw(newPassword, userRepository.findUserByLogin(testUserLogin).get().password))
    }

    @Test
    fun `updateUserPassword returns DIFF when passwords do not match`() {

        val plainPassword = userTestObject.password
        userTestObject = userTestObject.copy(password = BCrypt.hashpw(plainPassword, BCrypt.gensalt()))
        userRepository.save(userTestObject)

        val userData = UserPasswordUpdateRequest(
            "wrongPassword",
            "newPassword123",
        )
        val result = userService.updateUserPassword(userTestObject, userData)
        assertEquals("diff", result)
    }

    @Test
    fun `updateUserPassword updates user with new data`() {

        val plainPassword = userTestObject.password
        userTestObject = userTestObject.copy(password = BCrypt.hashpw(plainPassword, BCrypt.gensalt()))
        userRepository.save(userTestObject)

        val userData = UserPasswordUpdateRequest(
            plainPassword,
            "newPassword123",
            "Jane",
            "Doe",
            "jane.doe@example.com",
            "555-555-5555",
            "California"
        )
        val result = userService.updateUserPassword(userTestObject, userData)
        val updatedUser = userRepository.findUserByLogin(testUserLogin).get()
        assertEquals("ok", result)
        assertEquals("Jane", updatedUser.firstName)
        assertEquals("Doe", updatedUser.lastName)
        assertEquals("jane.doe@example.com", updatedUser.email)
        assertEquals("555-555-5555", updatedUser.phone)
        assertEquals("California", updatedUser.location)
        assertTrue(BCrypt.checkpw("newPassword123", userRepository.findUserByLogin(testUserLogin).get().password))
    }


}