package com.ktorjwt.plugins

import authenticate
import com.ktorjwt.data.user.UserDataSource
import com.ktorjwt.security.hashing.HashingService
import com.ktorjwt.security.token.TokenConfig
import com.ktorjwt.security.token.TokenService
import getUserIdFromToken
import io.ktor.server.application.*
import io.ktor.server.routing.*
import signIn
import signUp

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signUp(hashingService, userDataSource)
        signIn(hashingService, userDataSource, tokenService, tokenConfig)
        authenticate()
        getUserIdFromToken()
    }
}
