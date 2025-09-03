package com.bentley.auth.host

import com.bentley.core.NotFoundException
import org.springframework.stereotype.Service

@Service
class HostService(
    private val hostMapper: HostMapper,
) {

    fun create(host: Host) {
        hostMapper.insert(host)
    }

    fun get(id: Long): Host {
        return hostMapper.findById(id) ?: throw NotFoundException("Host $id not found")
    }

    fun getOrNullByEmail(email: String): Host? {
        return hostMapper.findByEmail(email)
    }
}