package com.asadmshah.rplace.server

import com.asadmshah.rplace.pubsub.PubSubClientModule
import io.undertow.Undertow

fun main(args: Array<String>) {
    val component = DaggerDIComponent
            .builder()
            .pubSubClientModule(PubSubClientModule())
            .build()

    Undertow.builder()
            .addHttpListener(80, "0.0.0.0")
            .setHandler(component.rootHandler())
            .build()
            .start()
}
