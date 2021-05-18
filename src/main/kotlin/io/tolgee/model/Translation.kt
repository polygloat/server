package io.tolgee.model

import io.tolgee.model.key.Key
import io.tolgee.service.dataImport.ImportService
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*

@Entity
@EntityListeners(Translation.Companion.TranslationListeners::class)
@Table(
        uniqueConstraints = [UniqueConstraint(
                columnNames = ["key_id", "language_id"],
                name = "translation_key_language"
        )]
)
data class Translation(
        @Column(columnDefinition = "text")
        var text: String? = null
) : StandardAuditModel() {
    @ManyToOne
    var key: Key? = null

    @ManyToOne
    var language: Language? = null

    constructor(text: String?, key: Key?, language: Language?) : this(text) {
        this.key = key
        this.language = language
    }

    class TranslationBuilder internal constructor() {
        private var id: Long? = null
        private var text: String? = null
        private var key: Key? = null
        private var language: Language? = null

        fun text(text: String?): TranslationBuilder {
            this.text = text
            return this
        }

        fun key(key: Key?): TranslationBuilder {
            this.key = key
            return this
        }

        fun language(language: Language?): TranslationBuilder {
            this.language = language
            return this
        }

        fun build(): Translation {
            return Translation(text, key, language)
        }

        override fun toString(): String {
            return "Translation.TranslationBuilder(id=$id, text=$text, key=$key, language=$language)"
        }
    }

    companion object {
        @JvmStatic
        fun builder(): TranslationBuilder {
            return TranslationBuilder()
        }

        @Configurable
        class TranslationListeners {

            @Autowired
            lateinit var provider: ObjectFactory<ImportService>

            @PreRemove
            @Transactional
            fun preRemove(translation: Translation) {
                provider.`object`.onTranslationConflictRemoved(translation)
            }
        }
    }
}
