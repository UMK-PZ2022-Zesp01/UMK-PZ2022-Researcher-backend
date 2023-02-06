package pl.umk.mat.zesp01.pz2022.researcher.idgenerator

import spock.lang.Specification

class IdGeneratorSpec extends Specification {

    def "should generate user id"() {
        given: "id generator"
            def idGenerator = new IdGenerator()

        and: "exiting user ids"
            def userIds = ['"{"id": "ABCDEFGH"}"']

        when: "new user id is generated"
            def newUserId = idGenerator.generateUserId(userIds)

        then: "new user id is unique"
            !newUserId.blank
            newUserId.length() == 8
            newUserId != "ABCDEFGH"
    }

    def "should fail generating user id for malformed input"() {
        given: "id generator"
            def idGenerator = new IdGenerator()

        when: "new user id is generated"
            idGenerator.generateUserId([userIds])

        then: "exception is thrown"
            thrown(RuntimeException)

        where:
            userIds << ['"ABCDEFGH"}', '"{"id": "ABC"}"']
    }

}