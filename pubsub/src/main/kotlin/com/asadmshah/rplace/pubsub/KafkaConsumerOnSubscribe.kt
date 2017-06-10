package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.Position
import io.reactivex.*
import io.reactivex.Observable
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.InterruptException
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal class KafkaConsumerOnSubscribe(private val properties: Properties,
                                        private val topic: String) : FlowableOnSubscribe<ConsumerRecords<Position, DrawEvent>> {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KafkaConsumerOnSubscribe::class.java)

        fun create(properties: Properties, topic: String, strategy: BackpressureStrategy, scheduler: Scheduler): Observable<ConsumerRecord<Position, DrawEvent>> {
            return Flowable.create(KafkaConsumerOnSubscribe(properties, topic), strategy)
                    .flatMapIterable { it }
                    .subscribeOn(scheduler)
                    .toObservable()
        }
    }

    override fun subscribe(emitter: FlowableEmitter<ConsumerRecords<Position, DrawEvent>>) {
        val isDisconnected = AtomicBoolean(false)

        LOGGER.info("Consumer Preparing...")
        isDisconnected.set(false)
        LOGGER.info("Consumer Prepared.")

        LOGGER.info("Consumer Connecting...")
        val consumer = KafkaConsumer<Position, DrawEvent>(properties)
        LOGGER.info("Consumer Connected.")

        emitter.setCancellable {
            isDisconnected.set(true)
            consumer.wakeup()
        }

        consumer.subscribe(listOf(topic))

        while (!isDisconnected.get()) {
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

                if (!isDisconnected.get()) {
                    emitter.onError(e)
                }
                break
            }

            if (!isDisconnected.get()) {
                emitter.onNext(records)
            }
        }

        LOGGER.info("Consumer Disconnecting...")
        try {
            consumer.close()
        } catch (ignored: Exception) {

        } finally {
            LOGGER.info("Consumer Disconnected.")
        }
    }
}