package de.haebich.examples.car

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AvroExampleServiceApplication

fun main(args: Array<String>) {
	runApplication<AvroExampleServiceApplication>(*args)
}
