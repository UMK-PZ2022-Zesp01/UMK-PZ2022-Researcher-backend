package pl.umk.mat.zesp01.pz2022.researcher.idgenerator

class IdGenerator {

    // userIdList in functions below (generate...Id) contains strings like
    // {"id": "ABCDEFGH"}
    // so we need to extract the 'ABCDEFGH' part with use of function 'retrieveIds'
    private fun retrieveIds(list: List<String>, idLength: Int): List<String> =
        list.map { id -> id.substring(9, 9 + idLength) }


    private fun generateRandomId(idLength: Int): String {
        var id = ""
        for (i in 1..idLength) {
            id += listOf(('A'..'Z'), ('a'..'z'), ('0'..'9')).flatten().random()
        }
        return id
    }

    // function generates ID that is not in list of used IDs
    private fun generateUniqueId(usedIds: List<String>, idLength: Int): String {
        var id: String
        do id = generateRandomId(idLength)
        while (usedIds.contains(id))
        return id
    }

    fun generateUserId(userIdList: List<String>): String =

        generateUniqueId(
            retrieveIds(userIdList, 8),
            8
        )

    fun generateResearchId(researchIdList: List<String>): String =
        generateUniqueId(
            retrieveIds(researchIdList, 6),
            6
        )

    fun generateTokenId(): String =
        generateRandomId(6)

    fun generatePhotoId(photoIdList: List<String>): String =
        generateUniqueId(
            retrieveIds(photoIdList, 6),
            6
        )
}