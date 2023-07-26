package com.ktorjwt

import com.ktorjwt.data.user.MongoUserDataSource
import com.ktorjwt.plugins.configureRouting
import com.ktorjwt.plugins.configureSecurity
import com.ktorjwt.plugins.configureSerialization
import com.ktorjwt.security.hashing.SHA256HashingService
import com.ktorjwt.security.token.JWTTokenService
import com.ktorjwt.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    EngineMain.main(args)

fun Application.module() {
    val name = "ktor-db"
    val psw = System.getenv("MONGODB_PASSWORD")

    val db = KMongo.createClient(
        connectionString = "mongodb+srv://riccardoforzan:$psw@test.6nhkeel.mongodb.net/$name?retryWrites=true&w=majority",
    ).coroutine.getDatabase(name)

    val userDataSource = MongoUserDataSource(db)

    val tokenService = JWTTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    configureSerialization()
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig)
    configureSecurity(tokenConfig)
}
