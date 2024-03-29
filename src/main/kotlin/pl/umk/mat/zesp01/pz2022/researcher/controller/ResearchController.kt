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
import kotlin.math.*

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

    @PutMapping("/research/{code}/update", consumes = ["application/json;charset=UTF-8"])
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

    @GetMapping("/research/{code}/enrollCheck")
    fun checkCurrentUserEnrollment(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            if (researchService.checkIfUserIsAlreadyOnParticipantsList(code, username))
                ResponseEntity.status(HttpStatus.OK).build()
            else ResponseEntity.status(HttpStatus.NO_CONTENT).build()

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/research/{code}/participants", produces = ["application/json;charset=UTF-8"])
    fun getAllResearchParticipants(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            val research = researchService.getResearchByCode(code).get()

            // Only research creator can perform action in this endpoint
            if (username != research.creatorLogin)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

            return ResponseEntity.status(HttpStatus.OK).body(
                Gson().toJson(researchService.getAllResearchParticipantsData(code))
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @DeleteMapping("/research/{code}/resign")
    fun deleteCurrentUserFromResearch(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            return if (researchService.removeUserFromResearch(username, code))
                ResponseEntity.status(HttpStatus.OK).build()
            else ResponseEntity.status(HttpStatus.CONFLICT).build()

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @DeleteMapping("/research/{code}/delete/user/{login}")
    fun deleteUserFromResearch(
        @PathVariable code: String,
        @PathVariable login: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            val research = researchService.getResearchByCode(code).get()

            // Only research creator can perform action in this endpoint
            if (username != research.creatorLogin)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

            researchService.removeUserFromResearch(login, code)

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build()

        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/research/all")
    fun getAllResearches(): ResponseEntity<List<Research>> =
        ResponseEntity.status(HttpStatus.OK).body(researchService.getAllResearches())


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
        @RequestParam(required = false) forMeOnly: Boolean = false,
        @RequestParam(required = false) availableOnly: Boolean = false,
        @RequestParam(required = false) form: String? = null,
        @RequestParam(required = false) distance: Int? = null,
        @RequestParam(required = false) minDate: String? = null,
        @RequestParam(required = false) maxDate: String? = null,
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
            if (!jwt.isNullOrEmpty()) {
                val username = refreshTokenService.verifyAccessToken(jwt)
                if (username != null) {
                    userService.getUserByLogin(username).orElseThrow()
                } else throw Error()
            } else throw Error()
        } catch (e: Error) {
            null
        }

        val age = if (user == null || !forMeOnly) null
        else abs(ChronoUnit.YEARS.between(LocalDateTime.now(), LocalDate.parse(user.birthDate).atTime(0, 0)).toInt())

        val gender = if (forMeOnly) user?.gender else null

        val login = if (forMeOnly) user?.login else null

        val sorter = when (sortBy) {
            "newest" -> ResearchSorter("_id", "DESC")
            "ending" -> ResearchSorter("endDate", "ASC")
            "starting" -> ResearchSorter("begDate", "ASC")
            else -> ResearchSorter("_id", "DESC")
        }

        val loggedIn = !user?.login.isNullOrEmpty()


        val filteredResearches = researchService.filterResearches(
            researchFilters = ResearchFilters(
                login = login,
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
            smallerFirstPage = loggedIn,
        )

        val from = user?.locationCoords?.map { s -> s.toDouble() }

        fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadiusKm = 6371.0

            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val sinLat = sin(dLat / 2)
            val sinLon = sin(dLon / 2)

            val a = sinLat * sinLat + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinLon * sinLon)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadiusKm * c
        }

        fun distanceCheck(item: Research): Boolean {
            if (item.location.form == "remote") return true
            val cords: List<Double> = item.location.place.split(' ').map { s -> s.toDouble() }

            if (distance == null) return true
            if (from.isNullOrEmpty()) return true

            if (distance > calculateDistance(from[0],from[1],cords[0],cords[1])) return true

            return false
        }

        val distanceFiltered = when (distance != null && distance > 0) {
            true -> filteredResearches.filter { item->distanceCheck(item)}
            false -> filteredResearches
        }

        val responseResearches: List<Any> =
            when (loggedIn) {
                true -> distanceFiltered.map { research -> research.toResearchResponse() }
                false -> distanceFiltered.map { research: Research -> research.toSafeResearchResponse() }
            }







        return ResponseEntity
            .status(HttpStatus.OK)
            .body(Gson().toJson(responseResearches))
    }

    @GetMapping("/research/code/{code}", produces = ["application/json;charset=UTF-8"])
    fun getResearchByCode(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> =
        try {
            val jwt = httpHeaders["Authorization"]?.get(0)
            val loggedIn = try {
                if (!jwt.isNullOrEmpty()) {
                    val username = refreshTokenService.verifyAccessToken(jwt)
                    if (username != null) {
                        val user = userService.getUserByLogin(username).orElse(null)
                        if (user != null) true else throw Error()
                    } else throw Error()
                } else throw Error()
            } catch (e: Error) {
                false
            }


            val research = researchService.getResearchByCode(code).orElseThrow()

            val researchResponse: Any = when (loggedIn) {
                true -> research.toResearchResponse()
                false -> research.toSafeResearchResponse()
            }

            ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(researchResponse))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }

    @GetMapping("/research/creator/{creatorLogin}", produces = ["application/json;charset=UTF-8"])
    fun getResearchesByCreatorLogin(@PathVariable creatorLogin: String): ResponseEntity<String> =
        try {
            val researches = researchService.getResearchesByCreatorLogin(creatorLogin).get()
            if (researches.isEmpty()) throw Exception()
            val researchResponseList = researches.map { research -> research.toResearchResponse() }
            ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(researchResponseList))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).body(Gson().toJson(listOf<String>()))
        }

    @GetMapping("/user/{login}/enrolledresearches", produces = ["application/json;charset=UTF-8"])
    fun getResearchesEnrolledByLogin(@PathVariable login: String): ResponseEntity<String> =
        try {
            val researches = researchService.getResearchesEnrolledByLogin(login).get()
            val researchResponseList = researches.map { research -> research.toResearchResponse() }

            ResponseEntity.status(HttpStatus.OK).body(Gson().toJson(researchResponseList))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }

    @DeleteMapping("/research/{code}/delete")
    fun deleteResearchByCode(
        @PathVariable code: String,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            // You can delete a research only if it's logged user's research
            if (!researchService.isGivenUserAResearchCreator(code, username))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

            researchService.deleteResearchByResearchCode(code)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}