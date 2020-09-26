package com.gmaniliapp

import com.gmaniliapp.route.authRoute
import com.gmaniliapp.route.notesRoute
import com.gmaniliapp.route.usersRoute
import com.gmaniliapp.service.login
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(CallLogging)
    install(Authentication) {
        configure()
    }
    install(Routing) {
        authRoute()
        usersRoute()
        notesRoute()
    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
}

private fun Authentication.Configuration.configure() {
    basic {
        realm = "Notes App Server"
        validate { credentials ->
            val email = credentials.name
            val password = credentials.password
            if (login(email, password).isOk()) {
                UserIdPrincipal(email)
            } else {
                null
            }
        }
    }
}
