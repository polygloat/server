package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.DbPopulatorReal
import io.tolgee.model.Organization
import io.tolgee.model.Permission
import io.tolgee.model.Repository
import io.tolgee.repository.OrganizationRepository
import io.tolgee.repository.PermissionRepository
import io.tolgee.repository.RepositoryRepository
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
        private val repositoryService: RepositoryService,
        private val repositoryRepository: RepositoryRepository,
        private val permissionService: PermissionService,
        private val userAccountRepository: UserAccountRepository,
        private val permissionRepository: PermissionRepository
) {
    @GetMapping(value = ["/create"])
    @Transactional
    open fun createRepositories() {
        users.forEach {
            dbPopulatorReal.createUserIfNotExists(username = it.email, name = it.name)
        }

        organizations.forEach {
            val organization = organizationRepository.save(Organization(
                    name = it.name,
                    addressPart = organizationService.generateAddressPart(it.name),
                    basePermissions = it.basePermission
            ))

            it.owners.forEach {
                userAccountService.getByUserName(it).get().let { user ->
                    organizationRoleService.grantOwnerRoleToUser(user, organization)
                }
            }

            it.members.forEach {
                userAccountService.getByUserName(it).get().let { user ->
                    organizationRoleService.grantMemberRoleToUser(user, organization)
                }
            }
        }

        repositories.forEach { repositoryData ->

            val userOwner = if (repositoryData.userOwner != null)
                userAccountService.getByUserName(repositoryData.userOwner).get() else null

            val organizationOwner = if (repositoryData.organizationOwner != null)
                organizationService.get(repositoryData.organizationOwner) else null


            val repository = repositoryRepository.save(Repository(
                    name = repositoryData.name,
                    addressPart = repositoryService.generateAddressPart(repositoryData.name),
                    userOwner = userOwner,
                    organizationOwner = organizationOwner
            ))

            repositoryData.permittedUsers.forEach {
                val user = userAccountService.getByUserName(it.userName).get()
                permissionRepository.save(Permission(repository = repository, user = user, type = it.permission))
            }
        }
    }

    @GetMapping(value = ["/clean"])
    @Transactional
    open fun cleanupRepositories() {
        repositories.forEach {
            repositoryService.deleteAllByName(it.name)
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
                val permission: Permission.RepositoryPermissionType,
        )

        data class UserData(
                val email: String,
                val name: String = email
        )

        data class OrganizationData(
                val basePermission: Permission.RepositoryPermissionType,
                val name: String,
                val owners: MutableList<String> = mutableListOf(),
                val members: MutableList<String> = mutableListOf(),
        )

        data class RepositoryDataItem(
                val name: String,
                val organizationOwner: String? = null,
                val userOwner: String? = null,
                val permittedUsers: MutableList<PermittedUserData> = mutableListOf(),
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
                        basePermission = Permission.RepositoryPermissionType.MANAGE,
                        owners = mutableListOf("cukrberg@facebook.com"),
                        members = mutableListOf("john@doe.com")
                ),
                OrganizationData(
                        name = "Microsoft",
                        basePermission = Permission.RepositoryPermissionType.EDIT,
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
                                        Permission.RepositoryPermissionType.TRANSLATE
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Word",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.RepositoryPermissionType.MANAGE
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Excel",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.RepositoryPermissionType.EDIT
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Powerpoint",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.RepositoryPermissionType.TRANSLATE
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Microsoft Frontpage",
                        organizationOwner = "microsoft",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "vaclav.novak@fake.com",
                                        Permission.RepositoryPermissionType.VIEW
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Vaclav's cool repository",
                        userOwner = "vaclav.novak@fake.com",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "cukrberg@facebook.com",
                                        Permission.RepositoryPermissionType.VIEW
                                )
                        )
                ),
                RepositoryDataItem(
                        name = "Vaclav's funny repository",
                        userOwner = "vaclav.novak@fake.com",
                        permittedUsers = mutableListOf(
                                PermittedUserData(
                                        "cukrberg@facebook.com",
                                        Permission.RepositoryPermissionType.MANAGE
                                )
                        )
                )
        )

        init {
            (1..20).forEach { number ->
                val email = "owner@zzzcool${number}.com";
                users.add(UserData(email))
            }
        }
    }
}