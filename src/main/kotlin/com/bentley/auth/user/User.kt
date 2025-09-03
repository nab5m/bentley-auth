package com.bentley.auth.user

import com.bentley.auth.core.Entity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class User(
    @field:NotBlank
    @field:Email
    val email: String, // TODO: 이메일 unique 제약 제거 후 중복 검사
    @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @field:NotBlank
    @field:Size(min = 8, max = 20)
    var password: String,
    val phone: String?,
    val firstName: String?,
    val lastName: String?,
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var status: Status = Status.PENDING,
    @JsonIgnore
    var deactivatedAt: LocalDateTime?,
): Entity() {

    enum class Status {
        PENDING,
        ACTIVE,
        DEACTIVE,
    }
}