package com.atombuilt.atomkt.spigot.example

import org.koin.core.component.KoinComponent

data class ExampleConfig(
    val welcomeMessage: String
) {

    companion object {

        val KoinComponent.exampleConfig: ExampleConfig get() = getKoin().get()
    }
}
