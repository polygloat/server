package io.tolgee.fixtures

import io.tolgee.model.UserAccount
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.ResultActions

@Component
@Scope("prototype")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class ProjectJwtAuthRequestPerformer(
        userAccount: UserAccount,
        projectUrlPrefix: String
) : ProjectAuthRequestPerformer(userAccount, projectUrlPrefix) {

    override fun performProjectAuthPut(url: String, content: Any?): ResultActions {
        return super.performAuthPut(projectUrlPrefix + project.id + "/" + url, content)
    }

    override fun performProjectAuthPost(url: String, content: Any?): ResultActions {
        return performAuthPost(projectUrlPrefix + project.id + "/" + url, content)
    }

    override fun performProjectAuthGet(url: String): ResultActions {
        return performAuthGet(projectUrlPrefix + project.id + "/" + url)
    }

    override fun performProjectAuthDelete(url: String, content: Any?): ResultActions {
        return performAuthDelete(projectUrlPrefix + project.id + "/" + url, content)
    }
}
