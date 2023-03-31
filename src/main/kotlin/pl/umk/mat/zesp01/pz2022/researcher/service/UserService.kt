package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*
import kotlin.collections.ArrayList

@Service
class UserService(
    @Autowired val userRepository: UserRepository,
    @Autowired val mongoOperations: MongoOperations
) {

    /*** ADD METHODS ***/

    fun addUser(user: User): User =
        userRepository.insert(user)

    /*** UPDATE METHODS ***/

    fun updateUserById(id: String, user: User) =
        mongoOperations.findAndReplace(
            Query.query(Criteria.where("_id").`is`(id)),
            user
        )

    fun updateUserByLogin(login: String,user: User) =
        mongoOperations.findAndReplace(
            Query.query(Criteria.where("login").`is`(login)),
            user
        )

    /*** DELETE METHODS ***/
    fun deleteUserById(id: String) =
        userRepository.deleteById(id)

    fun deleteUserByLogin(login: String) =
        userRepository.deleteByLogin(login)

    /*** GET METHODS ***/
    fun getAllUsers(): List<User> =
        userRepository.findAll()

    fun getAllUserIds(): List<String> =
        mongoOperations.aggregate(
            Aggregation.newAggregation(
                Aggregation.project("_id")
            ),
            "Users", String::class.java
        ).mappedResults

    fun getAllUserEmails():List<String>{
        val result= ArrayList<String>()
        val emailList=mongoOperations.aggregate(
                Aggregation.newAggregation(
                        Aggregation.project().andExclude("_id").andInclude("email")
                ),
                "Users", String::class.java
        ).mappedResults
        for (i in emailList){
            val temporaryEmail=i.substring(11).dropLast(2)
            println(temporaryEmail)
            result.add(temporaryEmail)
        }
        return result
    }
    fun getAllUserPhones():List<String>{
        val result= ArrayList<String>()
        val phoneList=mongoOperations.aggregate(
                Aggregation.newAggregation(
                        Aggregation.project().andExclude("_id").andInclude("phone")
                ),
                "Users", String::class.java
        ).mappedResults
        for (i in phoneList){
            val temporaryPhone=i.substring(11).dropLast(2)
            println(temporaryPhone)
            result.add(temporaryPhone)
        }
        return result
    }

    fun getUserById(id: String): Optional<User> =
        userRepository.findById(id)

    fun getUserByEmail(email: String): Optional<User> =
        userRepository.findUserByEmail(email)

    fun getUserByLogin(login: String): Optional<User> =
        userRepository.findUserByLogin(login)

    fun getUsersByFirstName(firstName: String): List<User> =
        userRepository.findUserByFirstName(firstName)
            .orElseThrow { throw RuntimeException("Cannot find User by First name") }

    fun getUsersByLastName(lastName: String): List<User> =
        userRepository.findUserByLastName(lastName)
            .orElseThrow { throw RuntimeException("Cannot find User by Last name") }

    // Made with MongoOperations
    fun findUsersByGender(gender: String): List<User> =
        mongoOperations.find(
            Query.query(Criteria.where("gender").`is`(gender)),
            "Users"
        )
}