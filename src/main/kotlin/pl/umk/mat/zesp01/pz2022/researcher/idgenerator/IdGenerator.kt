package pl.umk.mat.zesp01.pz2022.researcher.idgenerator

class IdGenerator {
    fun generateUserId(): String {
        var userId = ""
        for (i in 1..8)
            userId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
        return userId
    }

    fun generateResearchId(): String {
        var researchId = ""
        for (i in 1..6)
            researchId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
        return researchId
    }

    fun generateTokenId(): String {
        var tokenId = ""
        for (i in 1..6)
            tokenId += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
        return tokenId
    }

    // Function checks if 'id' is unique in list of all used ids (given as 'idList')
    // and returns true if 'id' is NOT in 'idList'
    fun checkIfUnique(id: String, idList: List<String>): Boolean =
        idList.stream().noneMatch { i -> i.equals(id) }
}