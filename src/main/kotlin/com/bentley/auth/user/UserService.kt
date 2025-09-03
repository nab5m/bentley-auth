package com.bentley.auth.user

import com.bentley.core.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val userMapper: UserMapper,
) {

    fun create(user: User): Int {
        return userMapper.insert(user)
    }

    fun update(user: User): Int {
        user.updatedAt = LocalDateTime.now()
        return userMapper.update(user)
    }

    fun deactivate(id: Long) {
        val user = get(id)
        user.status = User.Status.DEACTIVE
        userMapper.update(user)
    }

    fun get(id: Long): User {
        return userMapper.findById(id) ?: throw NotFoundException("User with id $id not found")
    }

    fun getOrNullByEmail(email: String): User? {
        return userMapper.findByEmail(email)
    }
}