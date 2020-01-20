package com.toyapp.pact.demo.creditcheck

import com.toyapp.pact.demo.common.withCustomConfiguration
import com.toyapp.pact.demo.creditcheck.CreditCheckConfig.segment
import com.toyapp.pact.demo.creditcheck.CreditCheckConfig.version
import com.toyapp.pact.demo.creditcheck.TestData.getAllContentOrById
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf

object MockHttpClient {

    private val headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))

    private val expr = "(/$version/$segment(/(\\d+))?)".toRegex()

    fun build() = HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                withCustomConfiguration()
            }
        }

        engine {
            addHandler { request ->
                val result: MatchResult? = expr.find(request.url.toString())
                val id: String? = result?.destructured?.component3()

                when {
                    // .../v1/customers
                    result != null && id.isNullOrEmpty() -> {
                        respond(
                                content = getAllContentOrById(),
                                headers = headers
                        )
                    }
                    // .../v1/customers/<id>
                    result != null && !id.isNullOrEmpty() -> {
                        respond(
                                content = getAllContentOrById(id.toInt()),
                                headers = headers
                        )
                    }
                    // others
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

}