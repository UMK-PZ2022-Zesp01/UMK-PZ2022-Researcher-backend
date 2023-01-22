package pl.umk.mat.zesp01.pz2022.researcher.users

import pl.umk.mat.zesp01.pz2022.researcher.model.User


class SampleUser {

    companion object {
        val userTestObject: User = User("_testID",
            "_testLOGIN",
            "testPASSWORD",
            "testFIRSTNAME",
            "testLASTNAME",
            "testEMAIL@test.com",
            "123456789",
            "01-01-1970",
            "Male",
            "testAVATARIMAGE.IMG")

        val updatedUserTestObject: User = userTestObject // data changes are in unit test
    }

}