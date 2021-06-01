package io.tolgee.service

import io.tolgee.constants.ApiScope
import io.tolgee.exceptions.PermissionException
import io.tolgee.model.ApiKey
import io.tolgee.model.Permission.ProjectPermissionType
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.security.AuthenticationFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SecurityService @Autowired constructor(private val authenticationFacade: AuthenticationFacade) {

    @set:Autowired
    lateinit var apiKeyService: ApiKeyService

    @set:Autowired
    lateinit var permissionService: PermissionService

    @Transactional
    fun grantFullAccessToRepo(project: Project?) {
        permissionService.grantFullAccessToRepo(activeUser, project)
    }

    fun checkAnyRepositoryPermission(projectId: Long): ProjectPermissionType {
        return getRepositoryPermission(projectId) ?: throw PermissionException()
    }

    fun checkRepositoryPermission(projectId: Long, requiredPermission: ProjectPermissionType): ProjectPermissionType {
        val usersPermission = checkAnyRepositoryPermission(projectId)
        if (requiredPermission.power > usersPermission.power) {
            throw PermissionException()
        }
        return usersPermission
    }

    fun checkApiKeyScopes(scopes: Set<ApiScope>, project: Project?) {
        if (!apiKeyService.getAvailableScopes(activeUser, project!!).containsAll(scopes)) {
            throw PermissionException()
        }
    }

    fun checkApiKeyScopes(scopes: Set<ApiScope>, apiKey: ApiKey) {
        checkApiKeyScopes(scopes, apiKey.project) // checks if user's has permissions to use api key with api key's permissions
        if (!apiKey.scopesEnum.containsAll(scopes)) {
            throw PermissionException()
        }
    }

    private fun getRepositoryPermission(projectId: Long): ProjectPermissionType? {
        return permissionService.getRepositoryPermissionType(projectId, activeUser)
    }

    private val activeUser: UserAccount
        get() = authenticationFacade.userAccount
}
