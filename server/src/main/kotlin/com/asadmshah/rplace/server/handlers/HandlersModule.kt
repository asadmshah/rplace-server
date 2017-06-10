package com.asadmshah.rplace.server.handlers

import dagger.Module
import dagger.Provides
import io.undertow.Handlers.websocket
import io.undertow.server.HttpHandler
import javax.inject.Named

@Module
class HandlersModule {

    companion object {
        const val KEY_CANVAS = "canvas"
        const val KEY_STREAM = "stream"
    }

    @Provides
    @Named(KEY_CANVAS)
    internal fun canvasHandler(handler: CanvasHandler): HttpHandler {
        return handler
    }

    @Provides
    @Named(KEY_STREAM)
    internal fun streamHandler(handler: StreamHandler): HttpHandler {
        return websocket(handler)
    }

}