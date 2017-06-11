package com.asadmshah.rplace.cache

import com.lambdaworks.redis.api.sync.RedisCommands
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

internal object BitmapRefresher {

    private val KEY_OFFSET = "offset".toByteArray()
    private val KEY_BITMAP = "bitmap".toByteArray()

    private val LOGGER = LoggerFactory.getLogger(BitmapRefresher::class.java)

    fun create(redis: RedisCommands<ByteArray, ByteArray>, computationScheduler: Scheduler, ioScheduler: Scheduler): Observable<Pair<Long, ByteArray>> {
        return Observable
                .interval(100, TimeUnit.MILLISECONDS, computationScheduler)
                .observeOn(ioScheduler)
                .doOnNext { LOGGER.info("Running: {}", it) }
                .map<Pair<Long?, ByteArray?>> {
                    redis.multi()
                    redis.get(KEY_OFFSET)
                    redis.get(KEY_BITMAP)
                    val result = redis.exec()
                    val pair = if (result[0] == null || result[1] == null) {
                        null to null
                    } else {
                        String(result[0] as ByteArray).toLong() to result[1] as ByteArray
                    }
                    pair
                }
                .observeOn(computationScheduler)
                .doOnError {
                    LOGGER.error("Unable to update.", it)
                }
                .onErrorResumeNext(Observable.empty())
                .filter { (offset, bitmap) -> offset != null && bitmap != null }
                .map { (offset, bitmap) -> offset!! to bitmap!! }
    }


}