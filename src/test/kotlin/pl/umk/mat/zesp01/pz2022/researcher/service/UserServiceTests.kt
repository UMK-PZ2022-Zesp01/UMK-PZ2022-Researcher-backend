package pl.umk.mat.zesp01.pz2022.researcher.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserServiceTests(
        @Autowired val userService: UserService,
        @Autowired val userRepository: UserRepository
) {

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun shouldAddUserCorrectly() {
        // given
        val userToAdd = User(firstName = "AAA", lastName = "BBB")

        // when
        userService.addUser(userToAdd)

        // then
        assertThat(userRepository.findAll().size).isEqualTo(1)
        val addedUser = userRepository.findAll()[0]
        assertThat(addedUser.firstName).isEqualTo("AAA")
        assertThat(addedUser.lastName).isEqualTo("BBB")
    }

    @Test
    fun shouldUpdateUserCorrectly() {
        // given
        val userToAdd = User(firstName = "AAA", lastName = "BBB")
        userRepository.save(userToAdd)
        val updatedUser = userToAdd.copy(firstName = "CCC", lastName = "DDD")

        // when
        userService.updateUser(updatedUser)

        // then
        val existingUser = userRepository.findAll()[0]
        assertThat(existingUser.firstName).isEqualTo("CCC")
        assertThat(existingUser.lastName).isEqualTo("DDD")
    }

}