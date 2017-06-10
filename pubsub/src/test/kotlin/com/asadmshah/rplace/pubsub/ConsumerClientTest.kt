package com.asadmshah.rplace.pubsub

import com.asadmshah.rplace.models.DrawEventsBatch
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConsumerClientTest {

    @Test
    fun receivePreviousEvents() {
        val scheduler = TestScheduler()

        val de1 = createConsumerRecord(1)
        val de2 = createConsumerRecord(2)
        val de3 = createConsumerRecord(3)
        val observable = Observable
                .just(de1, de2, de3)
                .concatMap { Observable.just(it).delay(1, TimeUnit.SECONDS, scheduler) }

        val observer = TestObserver<DrawEventsBatch>()
        ConsumerClient(observable, 4, scheduler).observe(-1).subscribe(observer)

        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(1)
        assertThat(observer.values().last()).isEqualTo(createDrawEventsBatch(1, 1))

        scheduler.advanceTimeTo(2, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(2)
        assertThat(observer.values().last()).isEqualTo(createDrawEventsBatch(2, 2))

        scheduler.advanceTimeTo(3, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(3)
        assertThat(observer.values().last()).isEqualTo(createDrawEventsBatch(3, 3))
    }

    @Test
    fun discardOldOnBufferCapacity() {
        val scheduler = TestScheduler()

        val de1 = createConsumerRecord(1)
        val de2 = createConsumerRecord(2)
        val de3 = createConsumerRecord(3)
        val observable = Observable
                .just(de1, de2, de3)

        val observer = TestObserver<DrawEventsBatch>()
        ConsumerClient(observable, 2, scheduler).observe(-1).subscribe(observer)

        scheduler.advanceTimeTo(2, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(1)
        assertThat(observer.values().last()).isEqualTo(createDrawEventsBatch(2, 3))
    }

    @Test
    fun onlyEmitGreaterThanOffsetEvents() {
        val scheduler = TestScheduler()

        val de1 = createConsumerRecord(1)
        val de2 = createConsumerRecord(2)
        val de3 = createConsumerRecord(3)
        val de4 = createConsumerRecord(4)
        val observable = Observable
                .just(de1, de2, de3, de4)

        val observer = TestObserver<DrawEventsBatch>()
        ConsumerClient(observable, 4, scheduler).observe(2).subscribe(observer)

        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(1)
        assertThat(observer.values().last()).isEqualTo(createDrawEventsBatch(3, 4))
    }

    @Test
    fun onlyEmitTheLatestValuePosition() {
        val scheduler = TestScheduler()

        val de1 = createConsumerRecord(1L, 1, 1, 10, 1L)
        val de2 = createConsumerRecord(2L, 2, 2, 20, 2L)
        val de3 = createConsumerRecord(3L, 2, 2, 21, 3L)
        val de4 = createConsumerRecord(4L, 1, 1, 11, 4L)
        val de5 = createConsumerRecord(5L, 2, 2, 22, 5L)
        val de6 = createConsumerRecord(6L, 1, 1, 12, 6L)
        val de7 = createConsumerRecord(7L, 3, 3, 30, 7L)
        val observable = Observable
                .just(de1, de2, de3, de4, de5, de6, de7)

        val observer = TestObserver<DrawEventsBatch>()
        ConsumerClient(observable, 16, scheduler)
                .observe(-1)
                .subscribe(observer)

        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        assertThat(observer.values()).hasSize(1)

        val value = observer.values().last()
        assertThat(value.offset).isEqualTo(7)
        assertThat(value.eventsCount).isEqualTo(3)

        // LinkedHashMap caveat -- When a key is reinserted, the position is unchanged.
        assertThat(value.eventsList)
                .containsExactly(
                        createDrawEvent(1, 1, 12, 6L),
                        createDrawEvent(2, 2, 22, 5L),
                        createDrawEvent(3, 3, 30, 7L)
                )
                .inOrder()
    }

}