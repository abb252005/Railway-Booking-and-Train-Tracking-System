package com.example.railway.domain.service

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.example.railway.domain.model.TrainPosition
import com.example.railway.util.currentTimeMillis

class SocketClient(
    private val host: String = "localhost",
    private val port: Int = 8080
) {
    private val selectorManager = SelectorManager(Dispatchers.Default)
    private var socket: Socket? = null
    
    private val _trainUpdates = MutableSharedFlow<TrainPosition>()
    val trainUpdates: SharedFlow<TrainPosition> = _trainUpdates.asSharedFlow()

    // Cache to rebuild full positions from deltas
    private val positionCache = mutableMapOf<String, TrainPosition>()

    suspend fun connect() {
        coroutineScope {
            try {
                socket = aSocket(selectorManager).tcp().connect(host, port)
                val receiveChannel = socket?.openReadChannel() ?: return@coroutineScope

                launch {
                    while (isActive) {
                        try {
                            val type = receiveChannel.readByte().toInt()
                            val length = receiveChannel.readInt()
                            
                            when (type) {
                                2 -> { // MSG_TYPE_TRAIN_POSITION (Full)
                                    val update = readFullPosition(receiveChannel)
                                    positionCache[update.trainId] = update
                                    _trainUpdates.emit(update)
                                }
                                3 -> { // MSG_TYPE_DELTA_UPDATE
                                    val trainId = readTrainId(receiveChannel)
                                    val mask = receiveChannel.readByte().toInt()
                                    val base = positionCache[trainId] ?: return@launch // Ignore delta if no base
                                    
                                    val updated = base.copy(
                                        latitude = if (mask and 1 != 0) receiveChannel.readDouble() else base.latitude,
                                        longitude = if (mask and 2 != 0) receiveChannel.readDouble() else base.longitude,
                                        progress = if (mask and 4 != 0) receiveChannel.readDouble() else base.progress,
                                        speedKmH = if (mask and 8 != 0) receiveChannel.readDouble() else base.speedKmH,
                                        bearing = if (mask and 16 != 0) receiveChannel.readDouble() else base.bearing,
                                        lastUpdateTime = currentTimeMillis()
                                    )
                                    positionCache[trainId] = updated
                                    _trainUpdates.emit(updated)
                                }
                                else -> receiveChannel.discard(length.toLong())
                            }
                        } catch (e: Exception) {
                            println("Socket read error: ${e.message}")
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                println("Connection failed: ${e.message}")
            }
        }
    }

    private suspend fun readTrainId(channel: ByteReadChannel): String {
        val bytes = ByteArray(32)
        channel.readFully(bytes)
        return bytes.decodeToString().takeWhile { it != '\u0000' }
    }

    private suspend fun readFullPosition(channel: ByteReadChannel): TrainPosition {
        val trainId = readTrainId(channel)
        return TrainPosition(
            trainId = trainId,
            latitude = channel.readDouble(),
            longitude = channel.readDouble(),
            progress = channel.readDouble(),
            speedKmH = channel.readDouble(),
            bearing = channel.readDouble(),
            lastUpdateTime = channel.readLong(),
            currentRouteId = null,
            nextDestinationStationId = null,
            estimatedTimeRemainingMinutes = 0.0
        )
    }

    fun disconnect() {
        socket?.dispose()
        selectorManager.close()
    }
}
