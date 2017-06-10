package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import io.reactivex.Observable
import org.slf4j.LoggerFactory

internal class PubSubClientImpl(val producer: ProducerClient, val consumer: ConsumerClient) : PubSubClient {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PubSubClientImpl::class.java)
    }

    override fun publish(event: DrawEvent) {
        producer.publish(event)
    }

    override fun subscribe(offset: Int): Observable<DrawEventsBatch> {
        return consumer.observe(offset)
    }
}