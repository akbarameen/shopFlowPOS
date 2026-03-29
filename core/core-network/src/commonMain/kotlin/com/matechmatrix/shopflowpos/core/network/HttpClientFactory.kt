package com.matechmatrix.shopflowpos.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                isLenient         = true
                ignoreUnknownKeys = true
                prettyPrint       = false
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level  = LogLevel.INFO
        }
    }
}