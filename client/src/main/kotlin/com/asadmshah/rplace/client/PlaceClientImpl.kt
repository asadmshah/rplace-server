package com.asadmshah.rplace.client

internal class PlaceClientImpl(private val host: String, private val port: Int) : PlaceClient {

    private val client = okhttp3.OkHttpClient()

    override fun canvas(): io.reactivex.Maybe<Pair<Long, ByteArray>> {
        return io.reactivex.Maybe
                .fromCallable {
                    val request = okhttp3.Request.Builder().url("http://$host:$port/canvas").build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) null else {
                            val offset = response.header("X-Offset", "0")!!.toLong()
                            val bitmap = response.body()!!.bytes()
                            offset to bitmap
                        }
                    }
                }
    }

    override fun stream(): io.reactivex.Observable<DrawingSocketEvents> {
        val request = okhttp3.Request.Builder().url("ws://$host:$port/stream").build()

        return io.reactivex.Flowable
                .create(com.asadmshah.rplace.server.client.WebSocketOnSubscribe(request), io.reactivex.BackpressureStrategy.BUFFER)
                .filter {
                    it is com.asadmshah.rplace.server.client.WebSocketEvent.OnOpen || it is com.asadmshah.rplace.server.client.WebSocketEvent.OnClosing || it is com.asadmshah.rplace.server.client.WebSocketEvent.OnBinaryMessage
                }
                .map<DrawingSocketEvents> {
                    when (it) {
                        is com.asadmshah.rplace.server.client.WebSocketEvent.OnOpen -> com.asadmshah.rplace.server.client.DrawingSocketEvents.OnOpened(it.webSocket)
                        is com.asadmshah.rplace.server.client.WebSocketEvent.OnClosing -> com.asadmshah.rplace.server.client.DrawingSocketEvents.OnClosed(it.webSocket)
                        is com.asadmshah.rplace.server.client.WebSocketEvent.OnBinaryMessage -> {
                            val event = com.asadmshah.rplace.models.DrawEventsBatch.parseFrom(it.bytes.toByteArray())
                            com.asadmshah.rplace.server.client.DrawingSocketEvents.OnDrawEvent(it.webSocket, event)
                        }
                        else -> {
                            null
                        }
                    }
                }
                .toObservable()
    }

}