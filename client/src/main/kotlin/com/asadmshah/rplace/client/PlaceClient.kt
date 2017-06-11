package com.asadmshah.rplace.client

interface PlaceClient {

    fun canvas(): io.reactivex.Maybe<Pair<Long, ByteArray>>

    fun stream(): io.reactivex.Observable<DrawingSocketEvents>

}