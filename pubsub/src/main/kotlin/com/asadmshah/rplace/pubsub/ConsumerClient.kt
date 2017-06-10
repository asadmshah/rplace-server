package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.asadmshah.rplace.models.Position
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal class ConsumerClient(observable: Observable<ConsumerRecord<Position, DrawEvent>>,
                              bufferSize: Int,
                              private val scheduler: Scheduler) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConsumerClient::class.java)
    }

    private val subject: Subject<Pair<Long, DrawEvent>> = ReplaySubject.createWithSize(bufferSize)

    init {
        observable.map { it.offset() to it.value() }.subscribe(subject)
    }

    fun observe(offset: Int): Observable<DrawEventsBatch> {
        return subject
                .observeOn(scheduler)
                .filter { offset < 0 || offset < it.first }
                .buffer(1, TimeUnit.SECONDS, scheduler)
                .filter { it.size > 0 }
                .map { items ->
                    val map = LinkedHashMap<Position, DrawEvent>()
                    items.forEach { item ->
                        map.put(item.second.position, item.second)
                    }

                    DrawEventsBatch
                            .newBuilder()
                            .addAllEvents(map.values)
                            .setOffset(items.last().first)
                            .build()
                }
    }

}