package com.bentley.auth.host

import org.apache.ibatis.annotations.Mapper

@Mapper
interface HostMapper {
    fun insert(host: Host): Int
    fun findById(id: Long): Host?
    fun findByEmail(email: String): Host?
}