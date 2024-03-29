package pl.umk.mat.zesp01.pz2022.researcher.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max


@Service
class ResearchService(
    @Autowired val researchRepository: ResearchRepository,
    @Autowired val userRepository: UserRepository,
    @Autowired val mongoOperations: MongoOperations,
) {

    fun addResearch(research: Research): Research {
        val updatedResearch = research.copy(
            researchCode = RandomStringUtils.random(8, true, true)
        )
        return researchRepository.insert(updatedResearch)
    }

    fun updateResearch(research: Research, updateData: ResearchUpdateRequest) {
        val updatedResearch = research.copy(
            title = updateData.title ?: research.title,
            description = updateData.description ?: research.description,
            begDate = updateData.begDate ?: research.begDate,
            endDate = updateData.endDate ?: research.endDate,
            creatorEmail = updateData.creatorEmail ?: research.creatorEmail,
            creatorPhone = updateData.creatorPhone ?: research.creatorPhone,
            participantLimit = updateData.participantLimit ?: research.participantLimit,
            location = updateData.location ?: research.location
        )

        mongoOperations.findAndReplace(
            Query.query(Criteria.where("researchCode").`is`(research.researchCode)),
            updatedResearch
        )
    }

    fun getAllResearchParticipantsData(researchCode: String): List<ParticipantsData> {
        val participants = researchRepository.findResearchByResearchCode(researchCode).get().participants
        val users = participants.map { user ->
            userRepository
                .findUserByLogin(user)
                .get()
                .toParticipantsData()
        }
        return users
    }

    fun addUserToParticipantsList(researchCode: String, login: String): String {
        val research = researchRepository.findResearchByResearchCode(researchCode).get()
        val participants: MutableList<String> = research.participants.toMutableList()

        if (research.creatorLogin == login) return "ERR_YOUR_RESEARCH"
        if (participants.contains(login)) return "ERR_ALREADY_IN_LIST"

        participants.add(login)
        val updatedResearch = research.copy(participants = participants)

        mongoOperations.findAndReplace(
            Query.query(Criteria.where("researchCode").`is`(researchCode)),
            updatedResearch
        )
        return "OK"
    }

    fun checkIfUserIsAlreadyOnParticipantsList(researchCode: String, login: String): Boolean =
        researchRepository
            .findResearchByResearchCode(researchCode)
            .get()
            .participants
            .contains(login)

    fun removeUserFromResearch(login: String, code: String): Boolean {
        val research = researchRepository.findResearchByResearchCode(code).get()
        val participants = research.participants.toMutableList()

        return if (participants.remove(login)) {
            val updatedResearch = research.copy(participants = participants)
            mongoOperations.findAndReplace(
                Query.query(Criteria.where("researchCode").`is`(research.researchCode)),
                updatedResearch
            )
            true
        } else false
    }

    fun removeUserFromAllResearches(login: String) {
        val researches = researchRepository.findAll()
        researches.forEach { research ->
            val participants = research.participants.toMutableList()

            if (participants.remove(login)) {
                val updatedResearch = research.copy(participants = participants)
                mongoOperations.findAndReplace(
                    Query.query(Criteria.where("researchCode").`is`(research.researchCode)),
                    updatedResearch
                )
            }
        }
    }

    fun getAllResearches(): List<Research> =
        researchRepository.findAll()

    fun getResearchByCode(code: String): Optional<Research> =
        researchRepository.findResearchByResearchCode(code)

    fun getResearchesByCreatorLogin(creatorLogin: String): Optional<List<Research>> =
        researchRepository.findAllByCreatorLogin(creatorLogin)

    fun getResearchesEnrolledByLogin(login: String): Optional<List<Research>> {
        val enrolledResearches = ArrayList<Research>()
        val researches = researchRepository.findAll()
        researches.forEach { research ->
            val participants = research.participants.toMutableList()

            if (participants.contains(login)) {
                enrolledResearches.add(research)
            }
        }
        return Optional.of(enrolledResearches)
    }

//	fun sortResearchesByTitle(): List<Research> =
//		mongoOperations.find(
//			Query().with(Sort.by(Sort.Direction.ASC, "title")),
//			Research::class.java
//		)

    fun isGivenUserAResearchCreator(code: String, login: String): Boolean =
        researchRepository.findResearchByResearchCode(code).get().creatorLogin == login

    fun deleteResearchByResearchCode(code: String) =
        researchRepository.deleteResearchByResearchCode(code)


    fun filterResearches(
        researchFilters: ResearchFilters,
        sorter: ResearchSorter,
        page: Int,
        perPage: Int,
        smallerFirstPage: Boolean,
    ): List<Research> {
        /** Age filter **/
        val ageFilter = if (researchFilters.age == null) Criteria()
        else Criteria().orOperator(
            Criteria.where("requirements").elemMatch(
                Criteria().andOperator(
                    Criteria.where("type").`is`("age"),
                    Criteria.where("criteria").elemMatch(
                        Criteria().andOperator(
                            Criteria.where("ageMin").lte(researchFilters.age),
                            Criteria.where("ageMax").gte(researchFilters.age)
                        )
                    )
                )
            ),
            Criteria.where("requirements").not().elemMatch(
                Criteria.where("type").`is`("age")
            )
        )

        /** Gender filter **/
        val genderFilter = if (researchFilters.gender == null) Criteria()
        else Criteria().orOperator(
            Criteria.where("requirements").elemMatch(
                Criteria().andOperator(
                    Criteria.where("type").`is`("gender"),
                    Criteria.where("criteria").elemMatch(
                        Criteria().`in`(researchFilters.gender)
                    )
                )
            ),
            Criteria.where("requirements").not().elemMatch(
                Criteria.where("type").`is`("gender")
            )
        )

        /** Filter out user's researches **/
        val authorFilter = if (researchFilters.login == null) Criteria()
        else Criteria.where("creatorLogin").ne(researchFilters.login)


        /** Research form filter **/
        val researchFormFilter = if (researchFilters.form == null) Criteria()
        else Criteria.where("location.form").`in`(researchFilters.form)

        /** Date filters **/
        val minDateFilter = if (researchFilters.minDate == null) Criteria()
        else Criteria.where("endDate").gte(researchFilters.minDate)

        val maxDateFilter = if (researchFilters.maxDate == null) Criteria()
        else Criteria.where("begDate").lte(researchFilters.maxDate)


        val dateFilter = Criteria().andOperator(
            minDateFilter,
            maxDateFilter,
        )


        /** Available-only filter **/
        val query = if (!researchFilters.availableOnly) Query()
        else
            BasicQuery(
                "{ ${"$"}expr: { ${"$"}lt: [ {${"$"}size: ${"\"\$participants\""}}, ${"\"\$participantLimit\""}  ] } }"
            )

        val researchSkip = max(0, ((page - 1) * perPage) - if (smallerFirstPage) 1 else 0)
        val researchLimit = perPage - if (smallerFirstPage && (page == 1)) 1 else 0

        return mongoOperations.find(
            query.addCriteria(
                Criteria().andOperator(
                    ageFilter,
                    genderFilter,
                    authorFilter,
                    researchFormFilter,
                    dateFilter,
                )
            ).with(
                Sort.by(
                    Sort.Direction.fromString(sorter.direction),
                    sorter.sortBy
                )
            )
                .skip(researchSkip.toLong())
                .limit(researchLimit),


            Research::class.java
        )
    }
}

