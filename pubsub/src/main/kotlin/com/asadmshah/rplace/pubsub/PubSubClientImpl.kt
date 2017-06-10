package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.asadmshah.rplace.models.Position
import io.reactivex.*
import io.reactivex.Observable
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.InterruptException
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal class PubSubClientImpl(producerProps: Properties,
                                private val consumerProps: Properties,
                                private val topic: String,
                                private val scheduler: Scheduler) : PubSubClient {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PubSubClientImpl::class.java)
    }

    private val producer: KafkaProducer<Position, DrawEvent> = KafkaProducer(producerProps)

    override fun publish(event: DrawEvent) {
        try {
            producer.send(ProducerRecord<Position, DrawEvent>(topic, event.position, event), null)
            LOGGER.info("DrawEvent published: {}", event)
        } catch (e: Exception) {
            LOGGER.error("Unable to send Kafka message", e)
        }
    }

    override fun subscribe(offset: Int, group: String): Observable<DrawEventsBatch> {
        return Flowable
                .create({ emitter: FlowableEmitter<ConsumerRecords<Position, DrawEvent>> ->
                    val disconnected = AtomicBoolean(false)

                    val consumer = KafkaConsumer<Position, DrawEvent>(Properties().apply {
                        putAll(consumerProps)
                        put("group.id", group)
                    })

                    emitter.setCancellable {
                        disconnected.set(true)
                    }

                    consumer.subscribe(listOf(topic))

                    while (!disconnected.get()) {
                        val records = try {
                            consumer.poll(500L).also {
                                consumer.commitSync()
                            }
                        } catch (ignored: WakeupException) {
                            break
                        } catch (ignored: InterruptException) {
                            break
                        } catch (ignored: InterruptedException) {
                            break
                        } catch (e: Exception) {
                            LOGGER.error("Unable to Poll Kafka", e)
                            if (!emitter.isCancelled) {
                                emitter.onError(e)
                            }
                            break
                        }

                        if (!emitter.isCancelled && records.count() > 0) {
                            emitter.onNext(records)
                        }
                    }

                    try {
                        consumer.wakeup()
                        consumer.close()
                    } catch (ignored: Exception) {

                    }
                }, BackpressureStrategy.BUFFER)
                .map {
                    val builder = DrawEventsBatch.newBuilder()
                    it.forEach {
                        builder.addEvents(it.value())
                        builder.offset = Math.max(it.offset(), builder.offset)
                    }
                    builder.build()
                }
                .toObservable()
                .subscribeOn(scheduler)
    }
}