package org.daiv

import org.daiv.websocket.BSDFrontendHeader
import org.daiv.websocket.ReceiveData
import org.daiv.websocket.SendData
import org.daiv.websocket.SendData3
import org.daiv.websocket.mh2.*

fun main() {
    DMHJSWebsocket(
        WebsocketBuilder(
            JSSendable(),
            messageFactory = DMHMessageFactory,
            requestHandler = listOf(DMHRequestHandler(BSDFrontendHeader.serializer(), SendData3.serializer()) { h, r ->
                println("received: $r")
            })
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