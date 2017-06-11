package com.asadmshah.rplace.cache

import io.reactivex.Single

interface BitmapCache {

    fun readLatest(): Single<Pair<Long, ByteArray>>

}