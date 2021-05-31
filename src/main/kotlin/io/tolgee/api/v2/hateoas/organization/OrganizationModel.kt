package io.tolgee.api.v2.hateoas.organization

import io.swagger.v3.oas.annotations.media.Schema
import io.tolgee.model.Permission
import io.tolgee.model.enums.OrganizationRoleType
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "organizations", itemRelation = "organization")
open class OrganizationModel(
        val id: Long,

        @Schema(example = "Beautiful organization")
        val name: String,

        @Schema(example = "btforg")
        val addressPart: String,

        @Schema(example = "This is a beautiful organization full of beautiful and clever people")
        val description: String?,
        val basePermissions: Permission.RepositoryPermissionType,

        val currentUserRole: OrganizationRoleType
) : RepresentationModel<OrganizationModel>()
