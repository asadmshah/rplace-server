package com.asadmshah.rplace.server.handlers

import dagger.Module
import dagger.Provides
import io.undertow.Handlers.websocket
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.BlockingHandler
import javax.inject.Named

@Module
class HandlersModule {

    @Provides
    @Named(CanvasHandler.KEY)
    internal fun canvasHandler(handler: CanvasHandler): HttpHandler {
        return BlockingHandler(handler)
    }

    @Provides
    @Named(StreamHandler.KEY)
    internal fun streamHandler(handler: StreamHandler): HttpHandler {
        return websocket(handler)
    }

    @Provides
    internal fun baseHandler(baseHandler: BaseHandler): HttpHandler {
        return baseHandler
    }

}