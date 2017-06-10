package com.asadmshah.rplace.server

import com.asadmshah.rplace.pubsub.PubSubClient
import com.asadmshah.rplace.pubsub.PubSubClientModule
import com.asadmshah.rplace.server.handlers.HandlersModule
import dagger.Component
import io.undertow.server.HttpHandler
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
        PubSubClientModule::class,
        HandlersModule::class
))
interface DIComponent {
    fun pubsub(): PubSubClient
    @Named(HandlersModule.KEY_CANVAS) fun canvasHandler(): HttpHandler
    @Named(HandlersModule.KEY_STREAM) fun streamHandler(): HttpHandler
}