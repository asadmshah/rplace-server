package com.asadmshah.rplace.cache

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import org.slf4j.LoggerFactory

internal class BitmapCacheImpl(observable: Observable<Pair<Long, ByteArray>>) : BitmapCache {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BitmapCacheImpl::class.java)
    }

    private val subject: Subject<Pair<Long, ByteArray>> = ReplaySubject.createWithSize(1)

    init {
        observable.subscribe(subject)
    }

    override fun readLatest(): Single<Pair<Long, ByteArray>> {
        return subject
                .take(1)
                .singleOrError()
    }
}