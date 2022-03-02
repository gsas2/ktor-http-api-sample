package com.jetbrains.handson.httpapi

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.jetbrains.handson.httpapi.routes.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.serialization.*
import java.util.concurrent.TimeUnit

data class JwtConfiguration(
    val issuer: String,
    val audience: String,
    val myRealm: String,
    val jwkProvider: JwkProvider
)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }

    val issuer = environment.config.property("jwt.issuer").getString()
    val jwtConfig = JwtConfiguration(
        issuer,
        environment.config.property("jwt.audience").getString(),
        environment.config.property("jwt.realm").getString(),
        JwkProviderBuilder(issuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    )

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.myRealm
            verifier(jwtConfig.jwkProvider, jwtConfig.issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.getClaim("name").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    registerLoginRoutes(jwtConfig)
    registerCustomerRoutes()
    registerOrderRoutes()
}
