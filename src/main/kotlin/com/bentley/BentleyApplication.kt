package com.bentley

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulith

@Modulith
@SpringBootApplication
class BentleyApplication

fun main(args: Array<String>) {
	runApplication<BentleyApplication>(*args)
}
