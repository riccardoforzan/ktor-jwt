import authenticate
import com.ktorjwt.data.requests.AuthRequest
import com.ktorjwt.data.responses.AuthResponse
import com.ktorjwt.data.user.User
import com.ktorjwt.data.user.UserDataSource
import com.ktorjwt.security.hashing.HashingService
import com.ktorjwt.security.hashing.SaltedHash
import com.ktorjwt.security.token.TokenClaim
import com.ktorjwt.security.token.TokenConfig
import com.ktorjwt.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        // Validate fields
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        if (areFieldsBlank) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)

        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        val isCreated = userDataSource.insertUser(user)
        if (!isCreated) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        // Validate fields
        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        if (areFieldsBlank) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isPasswordCorrect = hashingService.verify(
            value = request.password, saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isPasswordCorrect) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig, TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate(){
    authenticate {
        // The code inside this block is protected by the main authentication logic
        get("authenticate"){
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getUserIdFromToken(){
    authenticate(){
        get("user/id"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}
