package com.asadmshah.rplace.server

import com.asadmshah.rplace.pubsub.PubSubClientModule
import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.handlers.RequestDumpingHandler

fun main(args: Array<String>) {
    val component = DaggerDIComponent
            .builder()
            .pubSubClientModule(PubSubClientModule())
            .build()

    Undertow.builder()
            .addHttpListener(80, "0.0.0.0")
            .setHandler(RequestDumpingHandler(Handlers.path()
                    .addExactPath("/canvas", component.canvasHandler())
                    .addExactPath("/stream", component.streamHandler())
            ))
            .build()
            .start()
}
