package com.asadmshah.rplace.server.handlers

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.asadmshah.rplace.models.Position
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
        private val LOGGER = LoggerFactory.getLogger(StreamHandler::class.java)
    }

    override fun onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel) {
        var disposable: Disposable? = null

        channel.addCloseTask {
            disposable?.dispose()
        }

        disposable = pubSub
                .subscribe(-1, "${System.currentTimeMillis()}")
                .map {
                    Pair(it, ByteBuffer.wrap(it.toByteArray()))
                }
                .observeOn(Schedulers.from(channel.ioThread.worker))
                .subscribe(
                        { (events:DrawEventsBatch, _:ByteBuffer) ->
                            WebSockets.sendText(events.toString(), channel, null)
//                            WebSockets.sendBinary(byteBuffer, channel, null)
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
                        val buffer = toBuffer(*context.data.resource)
                        val event = DrawEvent.parseFrom(buffer)

                        pubSub.publish(event)
                        buffer.clear()
                    }

                    override fun onError(channel: WebSocketChannel, context: BufferedBinaryMessage, throwable: Throwable) {
                        LOGGER.error("onBinaryRead Error", throwable)

                        channel.close()
                    }
                })
            }

            override fun onText(webSocketChannel: WebSocketChannel, messageChannel: StreamSourceFrameChannel) {
                val data = BufferedTextMessage(64, false)
                data.read(messageChannel, object : WebSocketCallback<BufferedTextMessage> {
                    override fun complete(channel: WebSocketChannel, context: BufferedTextMessage) {
                        val split = context.data.split(":")
                        if (split.size == 4) {
                            val x = split[0].toInt()
                            val y = split[1].toInt()
                            val c = split[2].toInt()
                            val d = split[3].toLong()
                            val position = Position.newBuilder().setX(x).setY(y).build()
                            val event = DrawEvent.newBuilder().setPosition(position).setColor(c).setDatetime(d).build()

                            pubSub.publish(event)
                        }
                    }

                    override fun onError(channel: WebSocketChannel, context: BufferedTextMessage, throwable: Throwable) {
                        LOGGER.error("onTextRead Error", throwable)

                        channel.close()
                    }
                })
            }

            private fun toBuffer(vararg payload: ByteBuffer): ByteBuffer {
                if (payload.isEmpty()) return payload[0]

                val size = Buffers.remaining(payload).toInt()
                if (size == 0) {
                    return Buffers.EMPTY_BYTE_BUFFER
                }

                val buffer = ByteBuffer.allocate(size)
                payload.forEach { buffer.put(it) }
                buffer.flip()
                return buffer
            }
        })

        channel.resumeReceives()
    }
}