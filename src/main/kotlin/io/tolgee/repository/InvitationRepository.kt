package io.tolgee.repository

import io.tolgee.model.Invitation
import io.tolgee.model.Organization
import io.tolgee.model.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InvitationRepository : JpaRepository<Invitation?, Long?> {
  fun deleteAllByCreatedAtLessThan(date: Date)
  fun findOneByCode(code: String?): Optional<Invitation>
  fun findAllByPermissionProjectOrderByCreatedAt(project: Project): LinkedHashSet<Invitation>
  fun getAllByOrganizationRoleOrganizationOrderByCreatedAt(organization: Organization): List<Invitation>
}
