package com.asadmshah.rplace.client

object PlaceClientFactory {

    fun create(host: String, port: Int): PlaceClient {
        return PlaceClientImpl(host, port)
    }

}