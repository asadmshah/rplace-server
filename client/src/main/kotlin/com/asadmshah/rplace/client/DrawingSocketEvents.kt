package com.asadmshah.rplace.client

import com.asadmshah.rplace.models.DrawEventsBatch
import okhttp3.WebSocket

sealed class DrawingSocketEvents {
    class OnOpened(val webSocket: WebSocket) : DrawingSocketEvents()
    class OnDrawEvent(val webSocket: WebSocket, val events: DrawEventsBatch) : DrawingSocketEvents()
    class OnClosed(val webSocket: WebSocket) : DrawingSocketEvents()
}