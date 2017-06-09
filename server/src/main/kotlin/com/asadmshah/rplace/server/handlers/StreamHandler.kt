package com.asadmshah.rplace.server.handlers

import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.*
import io.undertow.websockets.spi.WebSocketHttpExchange
import org.slf4j.LoggerFactory

class StreamHandler: WebSocketConnectionCallback {
    override fun onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel) {
        LoggerFactory.getLogger(StreamHandler::class.java).debug("onConnect")
        channel.receiveSetter.set(object : AbstractReceiveListener() {
            override fun onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage) {
                LoggerFactory.getLogger(StreamHandler::class.java).debug("onFullTextMessage")
                WebSockets.sendText(message.data, channel, object : WebSocketCallback<Void> {
                    override fun complete(channel: WebSocketChannel, context: Void?) {
                        LoggerFactory.getLogger(StreamHandler::class.java).debug("onResponseComplete")
                    }

                    override fun onError(channel: WebSocketChannel, context: Void?, throwable: Throwable) {
                        LoggerFactory.getLogger(StreamHandler::class.java).debug("onResponseError", throwable)
                    }
                })
            }
        })
        channel.resumeReceives()
    }
}