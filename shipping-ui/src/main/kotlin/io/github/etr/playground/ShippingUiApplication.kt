package io.github.etr.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShippingUiApplication

fun main(args: Array<String>) {
    runApplication<ShippingUiApplication>(*args)
}