package pl.umk.mat.zesp01.pz2022.researcher.idgenerator

class IdGenerator {
    fun generateUserId(userIdList: List<String>): String {
        var userId: String
        // userIdList contains strings like: "{"id": "ABCDEFGH"}", so we need to extract the 'ABCDEFGH' part
        val userIdListSubstr = userIdList.map { id -> id.substring(9, 9+8) }

        do {
            userId = ""
            for (i in 1..8) {
                userId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
            }
        } while (userIdListSubstr.contains(userId))

        return userId
    }

    fun generateResearchId(researchIdList: List<String>): String {
        var researchId = ""
        val researchIdListSubstr = researchIdList.map { id -> id.substring(9, 9+6) }

        do {
            researchId = ""
            for (i in 1..6) {
                researchId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
            }
        } while (researchIdListSubstr.contains(researchId))

        return researchId
    }

    fun generateTokenId(): String {
        var tokenId = ""
        for (i in 1..6)
            tokenId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
        return tokenId
    }
}