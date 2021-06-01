package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.DbPopulatorReal
import io.tolgee.dtos.request.LanguageDTO
import io.tolgee.dtos.request.SetTranslationsDTO
import io.tolgee.dtos.request.SignUpDto
import io.tolgee.model.Organization
import io.tolgee.model.Permission
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.repository.OrganizationRepository
import io.tolgee.repository.PermissionRepository
import io.tolgee.repository.ProjectRepository
import io.tolgee.repository.UserAccountRepository
import io.tolgee.security.InternalController
import io.tolgee.service.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@Hidden
@RequestMapping(value = ["internal/e2e-data/repositories"])
@Transactional
@InternalController
open class RepositoriesE2eDataController(
        private val organizationService: OrganizationService,
        private val userAccountService: UserAccountService,
        private val dbPopulatorReal: DbPopulatorReal,
        private val organizationRoleService: OrganizationRoleService,
        private val organizationRepository: OrganizationRepository,
        private val projectService: ProjectService,
        private val projectRepository: ProjectRepository,
        private val permissionService: PermissionService,
        private val userAccountRepository: UserAccountRepository,
        private val permissionRepository: PermissionRepository,
        private val keyService: KeyService,
        private val languageService: LanguageService,
) {
    @GetMapping(value = ["/create"])
    @Transactional
    open fun createRepositories() {
        val createdUsers = mutableMapOf<String, UserAccount>()

        users.forEach {
            createdUsers[it.email] = userAccountService.dtoToEntity(
                    SignUpDto(
                            name = it.name, email = it.email, password = "admin")
            )
        }

        userAccountRepository.saveAll(createdUsers.values)


        organizations.forEach {
            val organization = organizationRepository.save(Organization(
                    name = it.name,
                    addressPart = organizationService.generateAddressPart(it.name),
                    basePermissions = it.basePermission
            ))

            it.owners.forEach {
                createdUsers[it]!!.let { user ->
                    organizationRoleService.grantOwnerRoleToUser(user, organization)
                }
            }

            it.members.forEach {
                createdUsers[it]!!.let { user ->
                    organizationRoleService.grantMemberRoleToUser(user, organization)
                }
            }
        }

        repositories.forEach { projectData ->

            val userOwner = if (projectData.userOwner != null)
                createdUsers[projectData.userOwner]!! else null

            val organizationOwner = if (projectData.organizationOwner != null)
                organizationService.get(projectData.organizationOwner) else null


            val project = projectRepository.save(Project(
                    name = projectData.name,
                    addressPart = projectService.generateAddressPart(projectData.name),
                    userOwner = userOwner,
                    organizationOwner = organizationOwner
            ))


            projectData.permittedUsers.forEach {
                val user = createdUsers[it.userName]!!
                permissionRepository.save(Permission(project = project, user = user, type = it.permission))
            }

            val createdLanguages = mutableListOf<String>()

            projectData.keyData.forEach {
                it.value.keys.forEach {
                    if (!createdLanguages.contains(it)) {
                        languageService.createLanguage(LanguageDTO(name = it, abbreviation = it), project)
                        createdLanguages.add(it)
                    }
                }
                keyService.create(project, SetTranslationsDTO(it.key, it.value))
            }
        }
    }

    @GetMapping(value = ["/clean"])
    @Transactional
    open fun cleanupRepositories() {
        projectService.deleteAllByName("I am a great project")

        repositories.forEach {
            projectService.deleteAllByName(it.name)
        }

        organizations.forEach {
            organizationService.deleteAllByName(it.name)
        }

        users.forEach {
            userAccountService.getByUserName(username = it.email).orElse(null)?.let {
                userAccountRepository.delete(it)
            }
        }
    }

    companion object {
        data class PermittedUserData(
                val userName: String,
                val permission: Permission.ProjectPermissionType,
        )

        data class UserData(
                val email: String,
                val name: String = email
        )

        data class OrganizationData(
                val basePermission: Permission.ProjectPermissionType,
                val name: String,
                val owners: MutableList<String> = mutableListOf(),
                val members: MutableList<String> = mutableListOf(),
        )

        data class RepositoryDataItem(
                val name: String,
                val organizationOwner: String? = null,
                val userOwner: String? = null,
                val permittedUsers: MutableList<PermittedUserData> = mutableListOf(),
                val keyData: Map<String, Map<String, String>> = mutableMapOf()
        )

        val users = mutableListOf(
                UserData("gates@microsoft.com", "Bill Gates"),
                UserData("evan@netsuite.com", "Evan Goldberg"),
                UserData("cukrberg@facebook.com", "Mark Cukrberg"),
                UserData("vaclav.novak@fake.com", "Vaclav Novak"),
                UserData("john@doe.com", "John Doe"),
        )

        val organizations = mutableListOf(
                OrganizationData(
                        name = "Facebook",
                        basePermission = Permission.ProjectPermissionType.MANAGE,
                        owners = mutableListOf("cukrberg@facebook.com"),
                        members = mutableListOf("john@doe.com")
                ),
                OrganizationData(
                        name = "Microsoft",
                        basePermission = Permission.ProjectPermissionType.EDIT,
                        owners = mutableListOf("gates@microsoft.com"),
                        members = mutableListOf("john@doe.com", "cukrberg@facebook.com")
                )
        )

        val repositories = mutableListOf(
                RepositoryDataItem(
                        name = "Facebook itself",
                        organizationOwner = "facebook",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.ProjectPermissionType.TRANSLATE
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Word",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.ProjectPermissionType.MANAGE
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Excel",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.ProjectPermissionType.EDIT
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Powerpoint",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.ProjectPermissionType.TRANSLATE
                                )
                        ),
                        keyData = mapOf(Pair("test", mapOf(Pair("en", "This is test text!"))))
                ),
                RepositoryDataItem(
                        name = "Microsoft Frontpage",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.ProjectPermissionType.VIEW
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Vaclav's cool project",
                        userOwner = "vaclav.novak@fake.com",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "cukrberg@facebook.com",
                                        Permission.ProjectPermissionType.VIEW
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Vaclav's funny project",
                        userOwner = "vaclav.novak@fake.com",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "cukrberg@facebook.com",
                                        Permission.ProjectPermissionType.MANAGE
                                )
                        )
                )
        )

        init {
            (1..20).forEach { number ->
                val email = "owner@zzzcool${number}.com";
                users.add(UserData(email))
                repositories.find { item -> item.name == "Microsoft Word" }!!.permittedUsers.add(
                        PermittedUserData(email, Permission.ProjectPermissionType.EDIT)
                )
            }
        }
    }
}
