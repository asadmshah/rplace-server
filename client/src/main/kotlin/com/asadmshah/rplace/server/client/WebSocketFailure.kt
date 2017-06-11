package com.asadmshah.rplace.server.client

import okhttp3.Response
import okhttp3.WebSocket

class WebSocketFailure(val webSocket: WebSocket, val response: Response, throwable: Throwable) : Throwable(throwable)
