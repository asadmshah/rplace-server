package com.asadmshah.rplace.client

internal class WebSocketOnSubscribe(private val request: okhttp3.Request): io.reactivex.FlowableOnSubscribe<Any> {

    override fun subscribe(emitter: io.reactivex.FlowableEmitter<Any>) {
        val client = okhttp3.OkHttpClient()

        val socket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                if (!emitter.isCancelled) {
                    emitter.onNext(com.asadmshah.rplace.server.client.WebSocketEvent.OnOpen(webSocket, response))
                }
            }

            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response) {
                if (!emitter.isCancelled) {
                    emitter.onError(com.asadmshah.rplace.server.client.WebSocketFailure(webSocket, response, t))
                }
            }

            override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(com.asadmshah.rplace.server.client.WebSocketEvent.OnClosing(webSocket, code, reason))
                }
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(com.asadmshah.rplace.server.client.WebSocketEvent.OnTextMessage(webSocket, text))
                }
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, bytes: okio.ByteString) {
                if (!emitter.isCancelled) {
                    emitter.onNext(com.asadmshah.rplace.server.client.WebSocketEvent.OnBinaryMessage(webSocket, bytes))
                }
            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(com.asadmshah.rplace.server.client.WebSocketEvent.OnClosed(webSocket, code, reason))
                    emitter.onComplete()
                }
            }
        })

        emitter.setCancellable {
            socket.cancel()
        }

    }

}