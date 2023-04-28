package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService
import pl.umk.mat.zesp01.pz2022.researcher.service.UserService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@RestController
class ResearchController(
    @Autowired val researchService: ResearchService,
    @Autowired val refreshTokenService: RefreshTokenService,
    @Autowired val userService: UserService,
) {
    @PostMapping(value = ["/research/add"], consumes = ["multipart/form-data"])
    fun addResearch(
        @RequestPart("researchProperties") researchRequest: ResearchRequest,
        @RequestPart("posterImage") posterImage: MultipartFile,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            val research = researchRequest.toResearch(posterImage)
            if (username != research.creatorLogin) throw Exception()

            val addedResearch = researchService.addResearch(research)
            ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(addedResearch.researchCode))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PutMapping("/research/{code}/update")
    fun updateResearch(
        @PathVariable code: String,
        @RequestBody researchUpdateData: ResearchUpdateRequest,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            val research = researchService.getResearchByCode(code).get()
            if (username != research.creatorLogin) throw Exception()

            researchService.updateResearch(research, researchUpdateData)
            ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: Exception) {
            when (e) {
                is NoSuchElementException -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                else -> ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        }
    }

    @PutMapping("/research/{code}/enroll")
    fun enrollOnResearch(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            val addResult = researchService.addUserToParticipantsList(code, username)
            if (addResult == "ERR_ALREADY_IN_LIST") return ResponseEntity.status(299).build()
            if (addResult == "ERR_YOUR_RESEARCH") return ResponseEntity.status(298).build()

            ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    //	@GetMapping("/research/all")
//	fun getAllResearches(): ResponseEntity<List<Research>> =
//		ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())


//    @GetMapping("/research/page/{page}/{perPage}", produces = ["application/json;charset=UTF-8"])
//    fun getAPageOfResearches(
//        @PathVariable page: Int,
//        @PathVariable perPage: Int
//    ): ResponseEntity<String> {
//        val allResearches = researchService.getAllResearches()
//        val length = allResearches.size
//
//        if (page > 0 && perPage > 0) {
//            val firstIndex = min(((page - 1) * perPage), length)
//            val lastIndex = min((page * perPage), length)
//
//            val responseBody = allResearches
//                .subList(firstIndex, lastIndex)
//                .map { research -> research.toResearchResponse() }
//
//            return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(Gson().toJson(responseBody))
//        }
//
//        return ResponseEntity
//            .status(HttpStatus.NO_CONTENT)
//            .build()
//    }


//	@GetMapping("/research/page/{page}/{perPage}/{filters}",produces = ["application/json;charset=UTF-8"])
//	fun getAPageOfResearches(
//		@PathVariable page: Int,
//		@PathVariable perPage: Int,
//		@PathVariable filters: List<String>
//	): ResponseEntity<String> {
//
//	}


    @GetMapping("/research", produces = ["application/json;charset=UTF-8"])
    fun getFilteredResearches(
        @RequestParam (required = false) forMeOnly: Boolean = false,
        @RequestParam (required = false) availableOnly: Boolean = false,
        @RequestParam (required = false) form: String? = null,
        @RequestParam (required = false) minDate: String? = null,
        @RequestParam (required = false) maxDate: String? = null,
        @RequestParam sortBy: String,
        @RequestParam page: Int,
        @RequestParam perPage: Int,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]?.get(0)

        if (page <= 0 && perPage <= 0) {
            return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build()
        }

        val user = try {
            if (jwt != null) {
                val username = refreshTokenService.verifyAccessToken(jwt)
                if (username != null) {
                    userService.getUserByLogin(username).orElseThrow()
                } else throw Error()
            } else throw Error()
        } catch (e: Error) {
            null
        }

        val age = if (user == null || !forMeOnly) null
        else abs(ChronoUnit.YEARS.between(LocalDateTime.now(), LocalDate.parse(user.birthDate).atTime(0,0)).toInt())

        val gender = if (forMeOnly) user?.gender else null

        val sorter = when (sortBy) {
            "newest" -> ResearchSorter("_id", "DESC")
            "ending" -> ResearchSorter("endDate", "ASC")
            "starting" -> ResearchSorter("begDate", "ASC")
            else -> ResearchSorter("_id", "DESC")
        }

        val researches = researchService.filterResearches(
            researchFilters = ResearchFilters(
                age = age,
                gender = gender,
                form = form?.split(","),
                minDate = minDate,
                maxDate = maxDate,
                availableOnly = availableOnly,
            ),
            sorter = sorter,
            page = page,
            perPage = perPage,
        ).map { research -> research.toResearchResponse() }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(Gson().toJson(researches))
    }


    @GetMapping("/research/code/{code}", produces = ["application/json;charset=UTF-8"])
    fun getResearchByCode(@PathVariable code: String): ResponseEntity<String> =
        try {
            val researchResponse = researchService.getResearchByCode(code).get().toResearchResponse()
            ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(researchResponse))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }

    @GetMapping("/research/creator/{creatorLogin}", produces = ["application/json;charset=UTF-8"])
    fun getResearchesByCreatorLogin(@PathVariable creatorLogin: String): ResponseEntity<String> =
        try {
            val researches = researchService.getResearchesByCreatorLogin(creatorLogin).get()
            val researchResponseList = researches.map { research -> research.toResearchResponse() }

            ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(researchResponseList))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }

//	@GetMapping("/research/all/sorted")
//	fun getSortedResearches(): ResponseEntity<List<Research>> =
//		ResponseEntity.status(HttpStatus.OK).body(researchService.sortResearchesByTitle())

    @DeleteMapping("/research/{code}/delete")
    fun deleteResearchById(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // usuwac badanie JEDYNIE gdy jest to badanie zalogowanego uzytkownika

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            researchService.deleteResearchByResearchCode(code)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}