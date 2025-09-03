package com.bentley.auth

import java.security.Principal

data class UserPrincipal(
    val id: Long,
): Principal {
    override fun getName(): String? {
        return id.toString()
    }
}