 package com.ktorjwt

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.ktorjwt.plugins.*
import org.litote.kmongo.KMongo

 fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val name = "ktor-db"
    val psw = System.getenv("MONGODB_PASSWORD")

    val db = KMongo.createClient(
         connectionString = "mongodb+srv://riccardoforzan:$psw@test.6nhkeel.mongodb.net/$name?retryWrites=true&w=majority",
    ).getDatabase(name)
    configureSecurity()
    configureSerialization()
    configureRouting()
}
