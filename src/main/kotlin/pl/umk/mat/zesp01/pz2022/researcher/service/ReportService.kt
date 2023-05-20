package pl.umk.mat.zesp01.pz2022.researcher.service

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Report
import pl.umk.mat.zesp01.pz2022.researcher.repository.ReportRepository

@Service
class ReportService(
    @Autowired val reportRepository: ReportRepository,
    @Autowired val mongoOperations: MongoOperations
) {

    fun addReport(report: Report): Report {

        return reportRepository.insert(report)
    }

    fun deleteReportById(id: String) = reportRepository.deleteById(id)

}