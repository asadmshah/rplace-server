package com.asadmshah.rplace.server.client

import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

sealed class WebSocketEvent {
    class OnOpen(val webSocket: WebSocket, val response: Response) : WebSocketEvent()
    class OnTextMessage(val webSocket: WebSocket, val text: String) : WebSocketEvent()
    class OnBinaryMessage(val webSocket: WebSocket, val bytes: ByteString) : WebSocketEvent()
    class OnClosing(val webSocket: WebSocket, val code: Int, val reason: String) : WebSocketEvent()
    class OnClosed(val webSocket: WebSocket, val code: Int, val reason: String) : WebSocketEvent()
}