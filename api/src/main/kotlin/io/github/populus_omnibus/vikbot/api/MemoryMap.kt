package io.github.populus_omnibus.vikbot.api

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


class MaintainEvent<K, T>(
    val delay: Duration = 15.minutes, val expireFunction: (K, T) -> Unit = { _, _ -> },
    val map: MutableMap<K, Pair<Instant, T>>
) {
    fun generateTimerEvent(): () -> Unit = object : () -> Unit {
        var lastMaintained = Clock.System.now()

        override fun invoke() {
            val now = Clock.System.now()
            lastMaintained = now
            map.entries.removeIf { (key, pair) ->
                (pair.first + delay < now).also {
                    if (it) expireFunction(key, pair.second)
                }
            }
        }
    }

    fun generateShutdownEvent(): () -> Unit = {
        map.entries.forEach { (key, pair) -> expireFunction(key, pair.second) }
    }
}

fun <K, T> createMemory(): MutableMap<K, Pair<Instant, T>> = mutableMapOf<K, Pair<Instant, T>>().synchronized()

fun <K, T> MutableMap<K, Pair<Instant, T>>.maintainEvent(delay: Duration = 15.minutes, expireFunction: (K, T) -> Unit = { _, _ -> }) =
    MaintainEvent(delay, expireFunction, this)

operator fun <K, T> MutableMap<K, Pair<Instant, T>>.set(key: K, value: T) {
    this[key] = Clock.System.now() to value
}

operator fun MutableMap<Long, Pair<Instant, Message>>.plusAssign(message: Message) {
    this[message.idLong] = Clock.System.now() to message
}

operator fun MutableMap<Long, Pair<Instant, Message>>.plusAssign(interaction: InteractionHook) {
    this += interaction.retrieveOriginal().complete()
}

operator fun <T : CustomMessageData> MutableMap<Long, Pair<Instant, T>>.plusAssign(value: T) {
    this[value.msg.idLong] = Clock.System.now() to value
}