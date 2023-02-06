package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import spock.lang.Specification

import static org.springframework.http.HttpMethod.POST

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class UserControllerSpec extends Specification {

    @Autowired
    UserRepository userRepository

    @Autowired
    TestRestTemplate restTemplate

    def setup() {
        userRepository.deleteAll()
    }

    def "should not add user when another user with login or email already exists"() {
        given: "user with given data exists"
            userRepository.save(initialUser)

        when: "another user with the same data is added"
            def response = restTemplate.exchange("/user/add", POST, new HttpEntity(userToAdd), String)

        then: "validation error is returned"
            response.statusCode.value() == expectedHttpStatusCode

        where:
            initialUser                                     | userToAdd                                       || expectedHttpStatusCode
            new User(email: "test@o2.pl", login: "test")    | new User(email: "test@o2.pl", login: "test")    || 299
            new User(email: "test@o2.pl", login: "test123") | new User(email: "test@o2.pl", login: "test456") || 299
            new User(email: "test123@o2.pl", login: "test") | new User(email: "test456@o2.pl", login: "test") || 298
    }

}
