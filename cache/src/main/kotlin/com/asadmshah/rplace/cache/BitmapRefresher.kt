package com.asadmshah.rplace.cache

import com.lambdaworks.redis.api.sync.RedisCommands
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

object BitmapRefresher {

    private val LOGGER = LoggerFactory.getLogger(BitmapRefresher::class.java)

    val KEY_OFFSET = "offset".toByteArray()
    val KEY_BITMAP = "bitmap".toByteArray()

    fun create(redis: RedisCommands<ByteArray, ByteArray>, computationScheduler: Scheduler, ioScheduler: Scheduler): Observable<Pair<Long, ByteArray>> {
        return Observable
                .interval(100, TimeUnit.MILLISECONDS, computationScheduler)
                .observeOn(ioScheduler)
                .map { readState(redis) }
                .observeOn(computationScheduler)
                .doOnError {
                    LOGGER.error("Unable to update.", it)
                }
                .onErrorResumeNext(Observable.empty())
                .filter { (offset, bitmap) -> offset != null && bitmap != null }
                .map { (offset, bitmap) -> offset!! to bitmap!! }
    }

    fun readState(redis: RedisCommands<ByteArray, ByteArray>): Pair<Long?, ByteArray?> {
        redis.multi()
        redis.get(KEY_OFFSET)
        redis.get(KEY_BITMAP)
        val result = redis.exec()
        if (result[0] == null || result[1] == null) {
            return null to null
        } else {
            return String(result[0] as ByteArray).toLong() to result[1] as ByteArray
        }
    }

}