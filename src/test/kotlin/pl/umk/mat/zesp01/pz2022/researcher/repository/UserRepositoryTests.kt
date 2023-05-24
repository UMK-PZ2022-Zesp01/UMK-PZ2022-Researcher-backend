package pl.umk.mat.zesp01.pz2022.researcher.repository

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

    @Autowired
    lateinit var userService: UserService
    @Autowired
    lateinit var userRepository: UserRepository
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
            location = "Bydgoszcz",
            isConfirmed = false
        )
        testUserLogin = userTestObject.login
        userRepository.deleteAll()
    }

    @Test
    fun `add new User by userRepository`() {
        // GIVEN (userTestObject)

        // WHEN
        userRepository.save(userTestObject)

        // THEN
        assertTrue(userTestObject == userService.getUserByLogin(testUserLogin).get())
    }

    @Test
    fun `delete existing User by userRepository`() {
        // GIVEN (userTestObject)
        userService.addUser(userTestObject)

        // WHEN
        userRepository.deleteUserByLogin(testUserLogin)

        // THEN
        assertTrue(userService.getUserByLogin(testUserLogin).isEmpty)
    }

    @Test
    fun `get user by login using userRepository`() {
        // GIVEN
        userService.addUser(userTestObject)

        // WHEN
        val result = userRepository.findUserByLogin(testUserLogin)

        // THEN
        assertEquals(userTestObject, result.get())
    }

}

