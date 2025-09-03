package com.bentley.core

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(
    override val message: String? = null,
    override val cause: Throwable? = null
): RuntimeException(message, cause)