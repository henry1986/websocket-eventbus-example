package org.daiv.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import mu.KotlinLogging
import org.daiv.websocket.*
import org.daiv.websocket.mh2.*
import java.time.Duration


class SimpleServer {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    val list = mutableMapOf<String, DMHSender<DMHKtorWebsocketHandler>>()

    fun start() {
        val s = embeddedServer(Netty, port = 8080) {
            install(WebSockets){
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
                            println("received from frontend: $d")
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
                    sender.send(BSDFrontendHeader.serializer(),SendData4.serializer(), BSDFrontendHeader(), SendData4("with Websocket", 56))
                    sender.send(BSDFrontendHeader.serializer(),SendData3.serializer(), BSDFrontendHeader(), SendData3("Hello World", 55))
                    println("sent")
                    z.listen()
                }
                static {
                    files("build/distributions")
                }
            }
        }
        s.start(true)
    }

    suspend fun broadcast(key:String){
        list[key]?.send(BSDFrontendHeader.serializer(),SendData3.serializer(), BSDFrontendHeader(), SendData3("Hello World", 55))
    }
}

fun main() {
    SimpleServer().start()
}