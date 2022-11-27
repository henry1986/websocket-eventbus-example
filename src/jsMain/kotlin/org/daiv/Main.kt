package org.daiv

import org.daiv.websocket.*
import org.daiv.websocket.mh2.*

fun main() {
    DMHJSWebsocket(
        WebsocketBuilder(
            JSSendable(),
            messageFactory = DMHMessageFactory,
            requestHandler = listOf(
                DMHRequestHandler(BSDFrontendHeader.serializer(), SendData4.serializer()) { h, r ->
                    println("received4: $r")
                },
                DMHWebsocketRequestHandler(BSDFrontendHeader.serializer(), SendData3.serializer()) { ws, h, r ->
                    println("received: $r")
                },
            )
        )
    ) {
        val sender = DMHSender(it)
        sender.send(
            BSDFrontendHeader.serializer(),
            SendData.serializer(),
            BSDFrontendHeader(),
            SendData("theString", 5),
            ReceiveData.serializer()
        ) { h, r ->
            println("received: $r")
        }
    }
}
