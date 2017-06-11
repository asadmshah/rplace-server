package com.asadmshah.rplace.server.client

import io.reactivex.Maybe
import io.reactivex.Observable

interface PlaceClient {

    fun canvas(): Maybe<Pair<Long, ByteArray>>

    fun stream(): Observable<DrawingSocketEvents>

}