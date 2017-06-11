package com.asadmshah.rplace.cache

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class BitmapCacheImplTest {

    @Test
    fun waitsOnInitialUpdate() {
        val scheduler = TestScheduler()

        val item1 = 1L to byteArrayOf(1)
        val observable = Observable
                .just(item1)
                .delay(1, TimeUnit.SECONDS, scheduler)

        val cache = BitmapCacheImpl(observable)

        val observer = TestObserver<Pair<Long, ByteArray>>()
        cache.readLatest().subscribe(observer)

        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS)
        observer.assertEmpty()
        observer.assertNotTerminated()
        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        observer.assertValue(item1)
        observer.assertComplete()
    }

    @Test
    fun returnsLatestUpdate() {
        val scheduler = TestScheduler()

        val item1 = 1L to byteArrayOf(1)
        val item2 = 2L to byteArrayOf(2)
        val item3 = 3L to byteArrayOf(3)
        val item4 = 4L to byteArrayOf(4)
        val observable = Observable
                .just(item1, item2, item3, item4)
                .concatMap { Observable.just(it).delay(1, TimeUnit.SECONDS, scheduler) }

        val cache = BitmapCacheImpl(observable)

        val observer = TestObserver<Pair<Long, ByteArray>>()

        scheduler.advanceTimeTo(3, TimeUnit.SECONDS)
        cache.readLatest().subscribe(observer)
        observer.assertValue(item3)
        observer.assertComplete()
    }

}