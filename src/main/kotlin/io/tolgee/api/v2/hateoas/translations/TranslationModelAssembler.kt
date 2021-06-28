package io.tolgee.api.v2.hateoas.translations

import io.tolgee.api.v2.controllers.V2TranslationsController
import io.tolgee.model.Translation
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.stereotype.Component

@Component
class TranslationModelAssembler : RepresentationModelAssemblerSupport<Translation, TranslationModel>(
        V2TranslationsController::class.java, TranslationModel::class.java) {
    override fun toModel(entity: Translation): TranslationModel {
        return TranslationModel(id = entity.id, text = entity.text, state = entity.state)
    }
}
