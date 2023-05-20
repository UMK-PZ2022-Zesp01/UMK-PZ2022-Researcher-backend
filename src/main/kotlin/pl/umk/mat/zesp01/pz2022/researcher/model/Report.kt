package pl.umk.mat.zesp01.pz2022.researcher.model
import org.springframework.data.mongodb.core.mapping.Document

@Document("Reports")
data class Report(
    val reportCode: String="",
    val reportMessage: String = "",
)