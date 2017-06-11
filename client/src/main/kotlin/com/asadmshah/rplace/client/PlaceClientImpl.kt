package com.asadmshah.rplace.client

import com.asadmshah.rplace.models.DrawEventsBatch
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request

internal class PlaceClientImpl(private val host: String, private val port: Int) : PlaceClient {

    private val client = OkHttpClient()

    override fun canvas(): Maybe<Pair<Long, ByteArray>> {
        return Maybe
                .fromCallable {
                    val request = Request.Builder().url("http://$host:$port/canvas").build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) null else {
                            val offset = response.header("X-Offset", "0")!!.toLong()
                            val bitmap = response.body()!!.bytes()
                            offset to bitmap
                        }
                    }
                }
    }

    override fun stream(offset: Long): Observable<DrawingSocketEvents> {
        val request = Request.Builder()
                .url("ws://$host:$port/stream")
                .header("X-Offset", offset.toString())
                .build()

        return Flowable
                .create(WebSocketOnSubscribe(request), BackpressureStrategy.BUFFER)
                .filter {
                    it is WebSocketEvent.OnOpen || it is WebSocketEvent.OnClosing || it is WebSocketEvent.OnBinaryMessage
                }
                .map<DrawingSocketEvents> {
                    when (it) {
                        is WebSocketEvent.OnOpen -> DrawingSocketEvents.OnOpened(it.webSocket)
                        is WebSocketEvent.OnClosing -> DrawingSocketEvents.OnClosed(it.webSocket)
                        is WebSocketEvent.OnBinaryMessage -> {
                            val event = DrawEventsBatch.parseFrom(it.bytes.toByteArray())
                            DrawingSocketEvents.OnDrawEvent(it.webSocket, event)
                        }
                        else -> {
                            null
                        }
                    }
                }
                .toObservable()
    }

}