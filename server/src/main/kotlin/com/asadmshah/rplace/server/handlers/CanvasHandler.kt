package com.asadmshah.rplace.server.handlers

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import javax.inject.Inject

internal class CanvasHandler @Inject constructor() : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
        exchange.responseSender.send("Hello World")
        exchange.endExchange()
    }
}