package com.asadmshah.rplace.server.client

import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import okhttp3.*
import okio.ByteString

internal class WebSocketOnSubscribe(private val request: Request): FlowableOnSubscribe<Any> {

    override fun subscribe(emitter: FlowableEmitter<Any>) {
        val client = OkHttpClient()

        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (!emitter.isCancelled) {
                    emitter.onNext(WebSocketEvent.OnOpen(webSocket, response))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response) {
                if (!emitter.isCancelled) {
                    emitter.onError(WebSocketFailure(webSocket, response, t))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(WebSocketEvent.OnClosing(webSocket, code, reason))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(WebSocketEvent.OnTextMessage(webSocket, text))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                if (!emitter.isCancelled) {
                    emitter.onNext(WebSocketEvent.OnBinaryMessage(webSocket, bytes))
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!emitter.isCancelled) {
                    emitter.onNext(WebSocketEvent.OnClosed(webSocket, code, reason))
                    emitter.onComplete()
                }
            }
        })

        emitter.setCancellable {
            socket.cancel()
        }

    }

}