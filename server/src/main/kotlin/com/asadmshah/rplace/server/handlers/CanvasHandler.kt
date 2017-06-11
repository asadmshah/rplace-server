package com.asadmshah.rplace.server.handlers

import com.asadmshah.rplace.cache.BitmapCache
import io.reactivex.schedulers.Schedulers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.HttpString
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class CanvasHandler @Inject constructor(private val bitmapCache: BitmapCache) : HttpHandler {

    companion object {
        const val KEY = "CanvasHandler"

        private val LOGGER = LoggerFactory.getLogger(CanvasHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange) {
        val (offset, bitmap) = bitmapCache.readLatest()
                .subscribeOn(Schedulers.computation())
                .timeout(1, TimeUnit.SECONDS, Schedulers.computation())
                .doOnError {
                    LOGGER.error("Unable to fetch bitmap.", it)
                }
                .doOnSuccess { (offset, bitmap) ->
                    LOGGER.info("Received Image with offset {} and size {}", offset, bitmap.size)
                }
                .blockingGet()

        exchange.responseHeaders.put(Headers.CONTENT_LENGTH, bitmap.size.toString())
        exchange.responseHeaders.put(Headers.CONTENT_TYPE, "image/png")
        exchange.responseHeaders.put(HttpString("X-Offset"), offset.toString())
        exchange.responseSender.send(ByteBuffer.wrap(bitmap))
    }

}