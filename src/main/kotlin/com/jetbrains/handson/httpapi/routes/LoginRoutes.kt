package com.jetbrains.handson.httpapi.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.*
import com.jetbrains.handson.httpapi.JwtConfiguration
import com.jetbrains.handson.httpapi.models.*
import com.jetbrains.handson.httpapi.utils.readPrivateKey
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File
import java.security.InvalidKeyException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

fun Route.wellKnownRoute() {
    static(".well-known") {
        staticRootFolder = File("certs")
        val jwksFileName = System.getenv("fileNameJWKS")
        file(jwksFileName)
    }
}

fun Route.loginRoute(config: JwtConfiguration) {
    post("/login") {
        val userCredentials = call.receive<User>()

        // Check username and password
        val user = userStorage.find { it.username == userCredentials.username && it.password == userCredentials.password } ?:
            return@post call.respondText(
                "Invalid Credentials Provided",
                status = HttpStatusCode.NotFound
            )

        val kid = System.getenv("kid")
        val publicKey = config.jwkProvider.get(kid).publicKey
        val privateKeyFileName = System.getenv("fileNamePrivateKey")
        val privateKey: RSAPrivateKey = readPrivateKey("certs/${privateKeyFileName}")
            ?: throw Exception("Private key file missing")
        val token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 120000)) // 60000 millis = 1 minute
            .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey))
        call.respond(hashMapOf("token" to token))
    }
}

fun Application.registerLoginRoutes(config: JwtConfiguration) {
    routing {
        wellKnownRoute()
        loginRoute(config)
    }
}

