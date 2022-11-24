package org.daiv.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import mu.KotlinLogging
import org.daiv.websocket.*
import org.daiv.websocket.mh2.*
import org.slf4j.event.Level
import java.time.Duration


class SimpleServer {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun start() {
        val s = embeddedServer(Netty, port = 8080) {
            install(CallLogging) {
                level = Level.DEBUG
            }
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(1)
            }
            routing {
                webSocket("/ws") {
                    val l =
                        listOf(DMHRequestResponse(
                            BSDFrontendHeader.serializer(),
                            SendData.serializer(),
                            ReceiveData.serializer()
                        ) { h, d ->
                            Message(h, ReceiveData(d.string + d.i))
                        }, DMHRequestResponse(
                            BSDFrontendHeader.serializer(),
                            SendData2.serializer(),
                            ReceiveData2.serializer()
                        ) { h, d ->
                            Message(h, ReceiveData2(d.string + d.i))
                        })

                    val z = DMHKtorWebsocketHandler(
                        WebsocketBuilder(
                            KtorSender(this),
                            messageFactory = DMHMessageFactory,
                            requestHandler = listOf(
                                DMHRequestHandler(
                                    BSDFrontendHeader.serializer(),
                                    SendData3.serializer()
                                ) { h, e ->

                                }),
                            requestResponses = l,
                            errorLogger = { it, t -> logger.error { "error in websocket: $it" } })
                    ) {
                        logger.error { "lost connection" }
                    }

                    // variable used to send something
                    val sender = DMHSender(z)

                    z.listen()
                }
            }
        }
    }
}
