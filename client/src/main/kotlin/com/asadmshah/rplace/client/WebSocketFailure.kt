package com.asadmshah.rplace.client

class WebSocketFailure(val webSocket: okhttp3.WebSocket, val response: okhttp3.Response, throwable: Throwable) : Throwable(throwable)
