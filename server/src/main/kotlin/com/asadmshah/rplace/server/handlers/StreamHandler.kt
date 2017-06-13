package com.asadmshah.rplace.server.handlers

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.pubsub.PubSubClient
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.*
import io.undertow.websockets.spi.WebSocketHttpExchange
import org.slf4j.LoggerFactory
import org.xnio.Buffers
import java.nio.ByteBuffer
import javax.inject.Inject

internal class StreamHandler @Inject constructor(private val pubSub: PubSubClient) : WebSocketConnectionCallback {

    companion object {
        const val KEY = "StreamHandler"

        private val LOGGER = LoggerFactory.getLogger(StreamHandler::class.java)
    }

    override fun onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel) {
        var disposable: Disposable? = null

        channel.addCloseTask {
            disposable?.dispose()
        }

        disposable = pubSub
                .subscribe(-1)
                .observeOn(Schedulers.from(channel.ioThread.worker))
                .subscribe(
                        { events ->
                            WebSockets.sendBinary(events.toByteString().asReadOnlyByteBuffer(), channel, null)
                        },
                        { throwable: Throwable ->
                            throwable.printStackTrace()
                            channel.close()
                        }
                )

        channel.receiveSetter.set(object : AbstractReceiveListener() {
            override fun onBinary(webSocketChannel: WebSocketChannel, messageChannel: StreamSourceFrameChannel) {
                val data = BufferedBinaryMessage(64, false)
                data.read(messageChannel, object : WebSocketCallback<BufferedBinaryMessage> {
                    override fun complete(channel: WebSocketChannel, context: BufferedBinaryMessage) {
                        val resource = context.data.resource
                        if (resource.isNotEmpty()) {
                            val size = Buffers.remaining(resource).toInt()
                            if (size > 0) {
                                val buffer = ByteBuffer.allocate(size)
                                resource.forEach { buffer.put(it) }
                                buffer.flip()

                                try {
                                    val event = DrawEvent.parseFrom(buffer)
                                    if (event.position.x in 0..1023 && event.position.y in 0..1023) {
                                        pubSub.publish(DrawEvent.parseFrom(buffer))
                                    }
                                } catch (e: Exception) {
                                    LOGGER.error("Unable to Publish Message.", e)
                                    channel.close()
                                } finally {
                                    buffer.clear()
                                }
                            }
                        }
                    }

                    override fun onError(channel: WebSocketChannel, context: BufferedBinaryMessage, throwable: Throwable) {
                        LOGGER.error("onBinaryRead Error", throwable)

                        channel.close()
                    }
                })
            }
        })

        channel.resumeReceives()
    }
}