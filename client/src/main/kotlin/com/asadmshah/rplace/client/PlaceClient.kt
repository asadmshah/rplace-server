package com.asadmshah.rplace.client

import io.reactivex.Maybe
import io.reactivex.Observable

interface PlaceClient {

    fun canvas(): Maybe<Pair<Long, ByteArray>>

    fun stream(offset: Long): Observable<DrawingSocketEvents>

}