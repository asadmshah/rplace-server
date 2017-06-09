package com.asadmshah.rplace.server

import com.asadmshah.rplace.server.handlers.CanvasHandler
import com.asadmshah.rplace.server.handlers.StreamHandler
import io.undertow.Handlers
import io.undertow.Handlers.websocket
import io.undertow.Undertow
import io.undertow.server.handlers.RequestDumpingHandler

fun main(args: Array<String>) {
    Undertow.builder()
            .addHttpListener(80, "0.0.0.0")
            .setHandler(RequestDumpingHandler(Handlers.path()
                    .addExactPath("/canvas", CanvasHandler())
                    .addExactPath("/stream", websocket(StreamHandler()))))
            .build()
            .start()
}
