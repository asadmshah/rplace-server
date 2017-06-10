package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.Position
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.util.*

internal class ProducerClient(properties: Properties, private val topic: String) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ProducerClient::class.java)
    }

    private val producer: KafkaProducer<Position, DrawEvent> = KafkaProducer(properties)

    fun publish(event: DrawEvent) {
        try {
            producer.send(ProducerRecord<Position, DrawEvent>(topic, event.position, event), null)
            LOGGER.info("DrawEvent published: x={}, y={}, c={}", event.position.x, event.position.y, event.color)
        } catch (e: Exception) {
            LOGGER.error("Unable to send Kafka message", e)
        }
    }

}