package com.asadmshah.rplace.server.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.RequestDumpingHandler
import javax.inject.Inject
import javax.inject.Named

internal class BaseHandler @Inject constructor(@Named(CanvasHandler.KEY) canvasHandler: HttpHandler,
                                               @Named(StreamHandler.KEY) streamHandler: HttpHandler) : HttpHandler {

    private val next = RequestDumpingHandler(Handlers.path()
            .addExactPath("/canvas", canvasHandler)
            .addExactPath("/stream", streamHandler)
    )

    override fun handleRequest(exchange: HttpServerExchange) {
        next.handleRequest(exchange)
    }
}