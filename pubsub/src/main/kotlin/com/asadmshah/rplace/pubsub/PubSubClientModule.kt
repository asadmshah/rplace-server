package com.asadmshah.rplace.pubsub

import dagger.Module
import dagger.Provides
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Singleton

@Module
class PubSubClientModule {

    @Provides
    @Singleton
    fun pubSubClient(): PubSubClient {
        return PubSubClientImpl(producerClient(), consumerClient())
    }

    private fun producerClient(): ProducerClient {
        val properties = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"))
            put("key.serializer", PositionSerializer::class.java.name)
            put("value.serializer", DrawEventSerializer::class.java.name)
            put("acks", "0")
            put("linger.ms", "500")
        }
        return ProducerClient(properties, System.getenv("KAFKA_TOPIC"))
    }

    private fun consumerClient(): ConsumerClient {
        val properties = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"))
            put("key.deserializer", PositionSerializer::class.java.name)
            put("value.deserializer", DrawEventSerializer::class.java.name)
            put("enable.auto.commit", "false")
            put("group.id", System.getenv("KAFKA_GROUP"))
        }
        val topic = System.getenv("KAFKA_TOPIC")
        val strategy = BackpressureStrategy.BUFFER
        val observable = KafkaConsumerOnSubscribe.create(properties, topic, strategy, Schedulers.single())
        return ConsumerClient(observable, 4096, Schedulers.computation())
    }

}