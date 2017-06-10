package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
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
        val producer = ProducerClient(producerProps, System.getenv("KAFKA_TOPIC"))

        val consumerProps = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"))
            put("key.deserializer", PositionSerializer::class.java.name)
            put("value.deserializer", DrawEventSerializer::class.java.name)
            put("enable.auto.commit", "false")
        }
        val subject = ReplaySubject.createWithSize<Pair<Long, DrawEvent>>(4096)
        // Start Consumer Thread.
        Thread(KafkaConsumerRunnable(consumerProps, System.getenv("KAFKA_TOPIC"), System.getenv("KAFKA_GROUP"), subject), "Kafka-Consumer-Thread").start()
        val consumer = ConsumerClient(subject, 64, Schedulers.computation())

        return PubSubClientImpl(producer, consumer)
    }

}