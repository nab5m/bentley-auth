package com.bentley

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import kotlin.test.Test

@SpringBootTest
class BentleyApplicationModuleTests {

    private val applicationModules = ApplicationModules.of(BentleyApplication::class.java)

    @Test
    fun verifyModules() {
        applicationModules.forEach { print(it) }
        applicationModules.verify()
        Documenter(applicationModules).writeDocumentation()
    }
}