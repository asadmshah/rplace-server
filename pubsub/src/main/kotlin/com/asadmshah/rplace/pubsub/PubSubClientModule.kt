package com.asadmshah.rplace.pubsub

import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Singleton

@Module
class PubSubClientModule {

    @Provides
    @Singleton
    fun pubSubClient(): PubSubClient {
        val producerProps = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"))
            put("key.serializer", PositionSerializer::class.java.name)
            put("value.serializer", DrawEventSerializer::class.java.name)
            put("acks", "0")
            put("linger.ms", "500")
        }

        val consumerProps = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"))
            put("key.deserializer", PositionSerializer::class.java.name)
            put("value.deserializer", DrawEventSerializer::class.java.name)
            put("enable.auto.commit", "false")
        }

        return PubSubClientImpl(producerProps, consumerProps, System.getenv("KAFKA_TOPIC"), Schedulers.io())
    }

}