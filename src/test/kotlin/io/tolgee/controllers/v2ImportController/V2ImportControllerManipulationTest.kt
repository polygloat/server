package io.tolgee.controllers.v2ImportController

import io.tolgee.assertions.Assertions.assertThat
import io.tolgee.controllers.SignedInControllerTest
import io.tolgee.development.testDataBuilder.data.ImportTestData
import io.tolgee.fixtures.andIsOk
import org.testng.annotations.Test

class V2ImportControllerManipulationTest : SignedInControllerTest() {
    @Test
    fun `it deletes import`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id

        logAsUser(user.username!!, "admin")

        performAuthDelete("/v2/repositories/${repositoryId}/import", null).andIsOk
        assertThat(importService.find(repositoryId, user.id!!)).isNull()
    }

    @Test
    fun `it deletes import language`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importEnglish.id}"
        performAuthDelete(path, null).andIsOk
        assertThat(importService.findLanguage(testData.importEnglish.id)).isNull()
    }

    @Test
    fun `it resolves import translation conflict (override)`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importEnglish.id}" +
                "/translations/${testData.translationWithConflict.id}/resolve/set-override"
        performAuthPut(path, null).andIsOk
        val translation = importService.findTranslation(testData.translationWithConflict.id)
        assertThat(translation?.resolvedHash).isTrue
        assertThat(translation?.override).isTrue
    }


    @Test
    fun `it resolves import translation conflict (keep)`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importEnglish.id}" +
                "/translations/${testData.translationWithConflict.id}/resolve/set-keep-existing"
        performAuthPut(path, null).andIsOk
        val translation = importService.findTranslation(testData.translationWithConflict.id)
        assertThat(translation?.resolvedHash).isTrue
        assertThat(translation?.override).isFalse
    }

    @Test
    fun `it resolves all language translation conflicts (override)`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importEnglish.id}/resolve-all/set-override"
        performAuthPut(path, null).andIsOk
        val translation = importService.findTranslation(testData.translationWithConflict.id)
        assertThat(translation?.resolvedHash).isTrue
        assertThat(translation?.override).isTrue
    }

    @Test
    fun `it resolves all language translation conflicts (keep)`() {
        val testData = ImportTestData()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importEnglish.id}/resolve-all/set-keep-existing"
        performAuthPut(path, null).andIsOk
        val translation = importService.findTranslation(testData.translationWithConflict.id)
        assertThat(translation?.resolvedHash).isTrue
        assertThat(translation?.override).isFalse
    }

    @Test
    fun `it selects language`() {
        val testData = ImportTestData()
        testData.setAllResolved()
        testData.setAllOverride()
        testDataService.saveTestData(testData.root)
        val user = testData.root.data.userAccounts[0].self
        val repositoryId = testData.repository.id
        logAsUser(user.username!!, "admin")
        val path = "/v2/repositories/${repositoryId}/import/result/languages/${testData.importFrench.id}/" +
                "select-existing/${testData.french.id}"
        performAuthPut(path, null).andIsOk
        assertThat(importService.findLanguage(testData.importFrench.id)?.existingLanguage)
                .isEqualTo(testData.french)
    }
}