package pl.umk.mat.zesp01.pz2022.researcher.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import pl.umk.mat.zesp01.pz2022.researcher.model.Report
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import pl.umk.mat.zesp01.pz2022.researcher.service.ReportService

@RestController
class ReportController(
    @Autowired val reportService: ReportService,
    @Autowired val refreshTokenService: RefreshTokenService
) {

    @PostMapping("/report/send", produces = ["application/json;charset:UTF-8"])
    fun addReport(
        @RequestBody report: Report,
        @RequestHeader httpHeaders: HttpHeaders
    ): ResponseEntity<String> {
        val jwt = httpHeaders["Authorization"]
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val username = refreshTokenService.verifyAccessToken(jwt[0]) ?: throw Exception()
            if (username.isEmpty()) throw Exception()

            reportService.addReport(report)

            return ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }
}