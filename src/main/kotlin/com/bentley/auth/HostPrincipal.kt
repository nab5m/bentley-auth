package com.bentley.auth

import java.security.Principal

data class HostPrincipal(
    val id: Long,
): Principal {
    override fun getName(): String? {
        return id.toString()
    }
}