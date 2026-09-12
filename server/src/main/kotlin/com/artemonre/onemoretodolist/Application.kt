package com.artemonre.onemoretodolist

import com.artemonre.onemoretodolist.observability.sentryDsn
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.sentry.Sentry

fun main() {
    // No-ops when sentryDsn is null (missing sentry.properties) - see sentry.properties.template.
    if (sentryDsn != null) {
        Sentry.init { options ->
            options.dsn = sentryDsn
            options.environment = "production"
        }
    }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            Sentry.captureException(cause)
            call.respondText(text = "Internal server error", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
    }
}