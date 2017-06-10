package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEvent
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.io.Closeable

class DrawEventSerializer : Serializer<DrawEvent>, Deserializer<DrawEvent>, Closeable, AutoCloseable {

    override fun deserialize(topic: String?, data: ByteArray): DrawEvent {
        return DrawEvent.parseFrom(data)
    }

    override fun serialize(topic: String?, data: DrawEvent): ByteArray {
        return data.toByteArray()
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {

    }

    override fun close() {

    }
}