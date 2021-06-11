package io.tolgee.controllers

import io.tolgee.ITest
import io.tolgee.annotations.ProjectApiKeyAuthTestMethod
import io.tolgee.dtos.request.LanguageDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.fixtures.*
import io.tolgee.helpers.JsonHelper
import org.assertj.core.api.Assertions
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testng.annotations.Test

@SpringBootTest
@AutoConfigureMockMvc
class LanguageControllerTest : ProjectAuthControllerTest("/v2/projects/"), ITest {
    private val languageDTO = LanguageDto("en", "en", "en")
    private val languageDTOBlank = LanguageDto(null, "")
    private val languageDTOCorrect = LanguageDto("Spanish", "Espanol", "es")

    @Test
    fun createLanguage() {
        val project = dbPopulator.createBase(generateUniqueString())
        createLanguageTestValidation(project.id)
        createLanguageCorrectRequest(project.id)
    }

    @Test
    fun editLanguage() {
        val test = dbPopulator.createBase(generateUniqueString())
        val en = test.getLanguage("en").orElseThrow { NotFoundException() }
        val languageDTO = LanguageDto(name = "newEnglish", tag = "newEn", originalName = "newOriginalEnglish",
                flagEmoji = "\uD83C\uDDEC\uD83C\uDDE7")
        performEdit(test.id, en.id, languageDTO).andIsOk.andAssertThatJson {
            node("name").isEqualTo(languageDTO.name)
            node("originalName").isEqualTo(languageDTO.originalName)
            node("tag").isEqualTo(languageDTO.tag)
            node("flagEmoji").isEqualTo(languageDTO.flagEmoji)
        }
        val dbLanguage = languageService.findByTag(languageDTO.tag, test.id)
        Assertions.assertThat(dbLanguage).isPresent
        Assertions.assertThat(dbLanguage.get().name).isEqualTo(languageDTO.name)
        Assertions.assertThat(dbLanguage.get().originalName).isEqualTo(languageDTO.originalName)
        Assertions.assertThat(dbLanguage.get().flagEmoji).isEqualTo(languageDTO.flagEmoji)
    }

    @Test
    fun findAllLanguages() {
        val project = dbPopulator.createBase(generateUniqueString(), "ben", "pwd")
        logAsUser("ben", "pwd")
        performFindAll(project.id).andIsOk.andPrettyPrint.andAssertThatJson {
            node("_embedded.languages") {
                isArray.hasSize(2)
            }
        }
    }

    @Test
    fun deleteLanguage() {
        val test = dbPopulator.createBase(generateUniqueString())
        val en = test.getLanguage("en").orElseThrow { NotFoundException() }
        performDelete(test.id, en.id).andExpect(MockMvcResultMatchers.status().isOk)
        Assertions.assertThat(languageService.findById(en.id!!)).isEmpty
        projectService.deleteProject(test.id)
    }

    @Test
    fun createLanguageTestValidationComa() {
        val project = dbPopulator.createBase(generateUniqueString())
        performCreate(
                project.id,
                LanguageDto(originalName = "Original name", name = "Name", tag = "aa,aa")
        ).andIsBadRequest.andAssertThatJson {
            node("STANDARD_VALIDATION.tag").isEqualTo("can not contain coma")
        }
    }

    private fun createLanguageCorrectRequest(repoId: Long) {
        performCreate(repoId, languageDTOCorrect).andIsOk.andAssertThatJson {
            node("name").isEqualTo(languageDTOCorrect.name)
            node("tag").isEqualTo(languageDTOCorrect.tag)
        }
        val es = languageService.findByTag("es", repoId)
        Assertions.assertThat(es).isPresent
        Assertions.assertThat(es.get().name).isEqualTo(languageDTOCorrect.name)
    }

    fun createLanguageTestValidation(repoId: Long) {
        val mvcResult = performCreate(repoId, languageDTO)
                .andExpect(MockMvcResultMatchers.status().isBadRequest).andReturn()
        Assertions.assertThat(mvcResult.response.contentAsString).contains("language_tag_exists")
        Assertions.assertThat(mvcResult.response.contentAsString).contains("language_name_exists")
        performCreate(repoId, languageDTOBlank).andIsBadRequest.andAssertThatJson {
            node("STANDARD_VALIDATION").apply {
                node("name").isEqualTo("must not be blank")
                node("tag").isEqualTo("must not be blank")
                node("originalName").isEqualTo("must not be blank")
            }
        }
    }

    @Test
    @ProjectApiKeyAuthTestMethod
    fun findAllLanguagesApiKey() {
        performProjectAuthGet("languages").andIsOk.andAssertThatJson {
            node("_embedded.languages").isArray.hasSize(2)
        }
    }

    private fun performCreate(projectId: Long, content: LanguageDto): ResultActions {

        return mvc.perform(
                LoggedRequestFactory.loggedPost("/v2/projects/$projectId/languages")
                        .contentType(MediaType.APPLICATION_JSON).content(
                                JsonHelper.asJsonString(content)))
    }

    private fun performEdit(projectId: Long, languageId: Long, content: LanguageDto): ResultActions {
        return mvc.perform(
                LoggedRequestFactory.loggedPut("/v2/projects/$projectId/languages/${languageId}")
                        .contentType(MediaType.APPLICATION_JSON).content(
                                JsonHelper.asJsonString(content)))
    }

    private fun performDelete(projectId: Long, languageId: Long): ResultActions {
        return mvc.perform(
                LoggedRequestFactory.loggedDelete("/v2/projects/$projectId/languages/$languageId")
                        .contentType(MediaType.APPLICATION_JSON))
    }

    private fun performFindAll(projectId: Long): ResultActions {
        return mvc.perform(
                LoggedRequestFactory.loggedGet("/v2/projects/$projectId/languages")
                        .contentType(MediaType.APPLICATION_JSON))
    }
}
