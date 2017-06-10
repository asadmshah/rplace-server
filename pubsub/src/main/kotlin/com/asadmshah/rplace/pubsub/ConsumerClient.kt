package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.ReplaySubject
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal class ConsumerClient(private val subject: ReplaySubject<Pair<Long, DrawEvent>>,
                              private val bufferSize: Int,
                              private val scheduler: Scheduler) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConsumerClient::class.java)
    }

    fun observe(offset: Int): Observable<DrawEventsBatch> {
        return subject
                .observeOn(scheduler)
                .filter { offset <= 0 || it.first > offset }
                .buffer(1, TimeUnit.SECONDS, scheduler, bufferSize)
                .filter { it.size > 0 }
                .map {
                    val builder = DrawEventsBatch.newBuilder()
                    it.forEach {
                        builder.addEvents(it.second)
                        builder.offset = Math.max(it.first, builder.offset)
                    }
                    builder.build()
                }
    }

}