package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.Position
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.io.Closeable

class PositionSerializer : Serializer<Position>, Deserializer<Position>, Closeable, AutoCloseable {
    override fun serialize(topic: String?, data: Position): ByteArray {
        return data.toByteArray()
    }

    override fun deserialize(topic: String?, data: ByteArray): Position {
        return Position.parseFrom(data)
    }

    override fun close() {

    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {

    }
}