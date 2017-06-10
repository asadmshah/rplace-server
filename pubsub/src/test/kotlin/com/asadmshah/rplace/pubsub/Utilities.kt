package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.asadmshah.rplace.models.Position
import org.apache.kafka.clients.consumer.ConsumerRecord

internal fun createDrawEventsBatch(a: Int, b: Int): DrawEventsBatch {
    return DrawEventsBatch
            .newBuilder()
            .addAllEvents(
                    (a..b).map { createDrawEvent(it, it, it, it.toLong()) }
            )
            .setOffset(b.toLong())
            .build()
}

internal fun createConsumerRecord(i: Int): ConsumerRecord<Position, DrawEvent> {
    return createConsumerRecord(i.toLong(), i, i, i, i.toLong())
}

internal fun createConsumerRecord(offset: Long, x: Int, y: Int, c: Int, d: Long): ConsumerRecord<Position, DrawEvent> {
    return ConsumerRecord("", 1, offset, createPosition(x, y), createDrawEvent(x, y, c, d))
}

internal fun createPosition(x: Int, y: Int): Position {
    return Position.newBuilder().setX(x).setY(y).build()
}

internal fun createDrawEvent(x: Int, y: Int, c: Int, d: Long): DrawEvent {
    return DrawEvent
            .newBuilder()
            .setPosition(createPosition(x, y))
            .setColor(c)
            .setDatetime(d)
            .build()
}
