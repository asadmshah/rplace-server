package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.Position
import io.reactivex.subjects.Subject
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.InterruptException
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

internal class KafkaConsumerRunnable(private val properties: Properties,
                                     private val topic: String,
                                     private val group: String,
                                     private val subject: Subject<Pair<Long, DrawEvent>>) : Runnable {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KafkaConsumerRunnable::class.java)
    }

    override fun run() {
        LOGGER.info("Consumer Preparing...")
        val isDisconnected = AtomicBoolean(false)
        val semaphore = Semaphore(1).apply { acquire() }
        LOGGER.info("Consumer Prepared.")

        LOGGER.info("Consumer Connecting...")
        val consumer = KafkaConsumer<Position, DrawEvent>(Properties().apply {
            putAll(properties)
            put("group.id", group)
        })
        LOGGER.info("Consumer Connected: {}", consumer)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                isDisconnected.set(true)
                consumer.wakeup()
                semaphore.acquire()
            }
        })

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
                    subject.onError(e)
                }
                break
            }

            records.forEach {
                subject.onNext(Pair(it.offset(), it.value()))
            }
        }

        try {
            consumer.close()
        } catch (ignored: Exception) {

        } finally {
            LOGGER.info("Consumer Disconnected: {}", consumer)
            semaphore.release()
        }

    }

}