package io.tolgee.dtos.request

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateRepositoryDTO(
        @field:NotNull @field:Size(min = 3, max = 500)
        var name: String? = null,

        @field:NotEmpty
        var languages: Set<LanguageDTO>? = null,

        @field:Size(min = 3, max = 60)
        @field:Pattern(regexp = "^[a-z0-9]*[a-z]+[a-z0-9]*$", message = "invalid_pattern")
        var addressPart: String? = null
)