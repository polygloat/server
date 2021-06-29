package io.tolgee.service

import io.tolgee.dtos.request.TranslationCommentDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.TranslationCommentState
import io.tolgee.model.translation.Translation
import io.tolgee.model.translation.TranslationComment
import io.tolgee.repository.translation.TranslationCommentRepository
import io.tolgee.security.AuthenticationFacade
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TranslationCommentService(
        private val translationCommentRepository: TranslationCommentRepository,
        private val authenticationFacade: AuthenticationFacade
) {
    @Transactional
    fun create(
            dto: TranslationCommentDto,
            translation: Translation,
            author: UserAccount = authenticationFacade.userAccount
    ): TranslationComment {
        return TranslationComment(
                text = dto.text,
                state = dto.state,
                author = author,
                translation = translation
        ).let {
            create(it)
        }
    }

    fun get(id: Long): TranslationComment {
        return translationCommentRepository.findById(id).orElseThrow { NotFoundException() }
    }

    @Transactional
    fun update(dto: TranslationCommentDto, entity: TranslationComment): TranslationComment {
        entity.text = dto.text
        entity.state = dto.state
        return this.update(entity)
    }

    @Transactional
    fun setState(entity: TranslationComment, state: TranslationCommentState): TranslationComment {
        entity.state = state
        return this.update(entity)
    }

    fun getPaged(translation: Translation, pageable: Pageable): Page<TranslationComment> {
        return translationCommentRepository.getPagedByTranslation(translation, pageable)
    }

    fun delete(entity: TranslationComment) {
        deleteByIds(listOf(entity.id))
    }

    @Transactional
    fun deleteByIds(ids: List<Long>) {
        return translationCommentRepository.deleteAllByIdIn(ids)
    }

    fun create(entity: TranslationComment): TranslationComment {
        return translationCommentRepository.save(entity)
    }

    fun update(
            entity: TranslationComment,
            updatedBy: UserAccount = authenticationFacade.userAccount
    ): TranslationComment {
        entity.updatedBy = updatedBy
        return translationCommentRepository.save(entity)
    }
}
