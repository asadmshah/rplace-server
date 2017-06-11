package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import io.reactivex.Observable

interface PubSubClient {

    fun publish(event: DrawEvent)

    fun subscribe(offset: Long = -1): Observable<DrawEventsBatch>
}