package org.daiv.websocket

import kotlinx.serialization.Serializable

@Serializable
class BSDFrontendHeader

@Serializable
data class SendData(val string:String, val i:Int)

@Serializable
data class ReceiveData(val answer:String)

@Serializable
data class SendData2(val string:String, val i:Int)

@Serializable
data class ReceiveData2(val answer:String)

@Serializable
data class SendData3(val string:String, val i:Int)
