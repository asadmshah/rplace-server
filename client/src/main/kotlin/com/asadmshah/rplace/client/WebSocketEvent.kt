package com.asadmshah.rplace.client

sealed class WebSocketEvent {
    class OnOpen(val webSocket: okhttp3.WebSocket, val response: okhttp3.Response) : com.asadmshah.rplace.client.WebSocketEvent()
    class OnTextMessage(val webSocket: okhttp3.WebSocket, val text: String) : com.asadmshah.rplace.client.WebSocketEvent()
    class OnBinaryMessage(val webSocket: okhttp3.WebSocket, val bytes: okio.ByteString) : com.asadmshah.rplace.client.WebSocketEvent()
    class OnClosing(val webSocket: okhttp3.WebSocket, val code: Int, val reason: String) : com.asadmshah.rplace.client.WebSocketEvent()
    class OnClosed(val webSocket: okhttp3.WebSocket, val code: Int, val reason: String) : com.asadmshah.rplace.client.WebSocketEvent()
}