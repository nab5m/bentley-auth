package com.bentley.auth.user

import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper {
    fun insert(user: User): Int
    fun update(user: User): Int
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
}