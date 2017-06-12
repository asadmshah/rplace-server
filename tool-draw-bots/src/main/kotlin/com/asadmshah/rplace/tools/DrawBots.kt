package com.asadmshah.rplace.tools

import com.asadmshah.rplace.client.DrawingSocketEvents
import com.asadmshah.rplace.client.PlaceClientFactory
import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.Position
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okio.ByteString
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main(args: Array<String>) {

    val LOGGER = LoggerFactory.getLogger("DrawBots")

    val host = System.getenv("ENDPOINT_HOST")
    val port = System.getenv("ENDPOINT_PORT").toInt()
    val outputDir = System.getenv("IMAGE_OUTPUT_DIR")
    val clientCount = System.getenv("CLIENT_COUNT").toInt()
    val intervalMillis = System.getenv("INTERVAL_MILLIS").toLong()

    val disposables = CompositeDisposable()
    val images = mutableListOf<BufferedImage>()

    for (clientIndex in 0 until clientCount) {
        LOGGER.info("Launching client #{}", clientIndex)
        val client = PlaceClientFactory.create(host, port)
        val (offset, bitmap) = client.canvas().blockingGet()

        val image = ByteArrayInputStream(bitmap).use {
            ImageIO.read(it)
        }
        images.add(image)

        client.stream(offset)
                .subscribeOn(Schedulers.from(Executors.newSingleThreadScheduledExecutor()))
                .subscribe(object : Observer<DrawingSocketEvents> {
                    override fun onComplete() {
                        LOGGER.info("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        LOGGER.error("onError", e)
                    }

                    override fun onNext(event: DrawingSocketEvents) {
                        when (event) {
                            is DrawingSocketEvents.OnOpened -> {
                                LOGGER.info("DrawingSocketEvent#OnOpened")
                                val random = Random()
                                disposables.add(Observable
                                        .interval(intervalMillis, TimeUnit.MILLISECONDS, Schedulers.computation())
                                        .observeOn(Schedulers.io())
                                        .subscribe {
                                            val x = random.nextPixel()
                                            val y = random.nextPixel()
                                            val c = random.nextColor()
                                            val d = System.currentTimeMillis()
                                            val data = createEvent(x, y, c, d)

                                            event.webSocket.send(data)
                                        })
                            }
                            is DrawingSocketEvents.OnClosed -> {
                                LOGGER.info("DrawingSocketEvent#OnClosed")
                            }
                            is DrawingSocketEvents.OnDrawEvent -> {
                                LOGGER.info("DrawingSocketEvent#OnDrawEvent")
                                event.events.eventsList.forEach {
                                    val x = it.position.x
                                    val y = it.position.y
                                    val c = it.color
                                    image.setRGB(x, y, c)
                                }
                            }
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        disposables.add(d)
                    }
                })
    }

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            disposables.dispose()

            images.forEachIndexed { i, image ->
                val dir = File("$outputDir/${System.getenv("HOSTNAME")}")
                dir.mkdirs()
                val file = File(dir, "$i.png")
                FileOutputStream(file).use { output ->
                    ImageIO.write(image, "png", output)
                }
                LOGGER.info("Saved Image: ${file.absolutePath}")
            }
        }
    })

}

fun Random.clampedInt(n: Int): Int {
    return Math.abs(nextInt()) % n
}

fun Random.nextPixel(): Int {
    return clampedInt(1024)
}

fun Random.nextColor(): Int {
    return Color(clampedInt(255), clampedInt(255), clampedInt(255)).rgb
}

fun createEvent(x: Int, y: Int, c: Int, d: Long): ByteString {
    val position = Position.newBuilder().setX(x).setY(y).build()
    val event = DrawEvent.newBuilder().setPosition(position).setColor(c).setDatetime(d).build()
    return ByteString.of(ByteBuffer.wrap(event.toByteArray()))
}
