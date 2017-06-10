package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import io.reactivex.Observable

interface PubSubClient {

    fun publish(event: DrawEvent)

    fun subscribe(offset: Int = -1, group: String): Observable<DrawEventsBatch>
}