package com.asadmshah.rplace.cache

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisURI
import com.lambdaworks.redis.api.StatefulRedisConnection
import com.lambdaworks.redis.codec.ByteArrayCodec
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class CacheModule {

    @Provides
    @Singleton
    fun redisClient(): StatefulRedisConnection<ByteArray, ByteArray> {
        val uri = RedisURI.Builder
                .redis(System.getenv("REDIS_HOST"), System.getenv("REDIS_PORT").toInt())
                .withPassword(System.getenv("REDIS_PASS"))
                .withDatabase(System.getenv("REDIS_DATABASE").toInt())
                .build()
        return RedisClient.create(uri).connect(ByteArrayCodec())
    }

    @Provides
    @Singleton
    fun bitmapCache(redis: StatefulRedisConnection<ByteArray, ByteArray>): BitmapCache {
        val scheduler = Schedulers.from(Executors.newSingleThreadScheduledExecutor())
        val refresher = BitmapRefresher.create(redis.sync(), Schedulers.computation(), scheduler)
        return BitmapCacheImpl(refresher)
    }

}