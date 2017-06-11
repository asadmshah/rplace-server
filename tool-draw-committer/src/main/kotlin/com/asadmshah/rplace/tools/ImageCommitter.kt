package com.asadmshah.rplace.tools

import com.asadmshah.rplace.cache.BitmapRefresher
import com.asadmshah.rplace.cache.CacheModule
import com.asadmshah.rplace.pubsub.PubSubClientModule
import com.lambdaworks.redis.api.sync.RedisCommands
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun main(args: Array<String>) {

    val LOGGER = LoggerFactory.getLogger("ImageCommiter")

    // Cheating.
    val consumerClient = PubSubClientModule().pubSubClient()
    val redis = CacheModule().redisClient().sync()

    // Retrieve the previous state.
    var (offset, bitmap) = BitmapRefresher.readState(redis)
    if (offset == null || bitmap == null) {
        offset = 0L

        // Empty Image.
        val image = BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            paint = Color(255, 255, 255)
            fillRect(0, 0, image.width, image.height)
        }
        bitmap = image.toByteArray()
        redis.updateState(offset, bitmap)
    }

    // Read Image.
    val image = ByteArrayInputStream(bitmap).use {
        ImageIO.read(it)
    }

    // Begin Polling Kafka.
    consumerClient
            .subscribe(offset)
            .doOnNext {
                LOGGER.info("Received new draw events | Count: {}", it.eventsCount)
            }
            .blockingForEach {
                it.eventsList.forEach {
                    image.setRGB(it.position.x, it.position.y, it.color)
                }
                LOGGER.info("Updated Local Image.")
                redis.updateState(it.offset, image.toByteArray())
                LOGGER.info("Committed to Redis.")
            }

}

fun BufferedImage.toByteArray(): ByteArray {
    val image = this
    return ByteArrayOutputStream().use { stream ->
        ImageIO.write(image, "png", stream)
        stream.toByteArray()
    }
}

fun RedisCommands<ByteArray, ByteArray>.updateState(offset: Long, bitmap: ByteArray) {
    multi()
    set(BitmapRefresher.KEY_OFFSET, offset.toString().toByteArray())
    set(BitmapRefresher.KEY_BITMAP, bitmap)
    exec()
}
