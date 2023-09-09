package io.github.populus_omnibus.vikbot.bot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File


@Serializable
data class BotConfig(
    val token: String,
    val botName: String = "VIKBOT",
    val initActivity: String = "with the old bot",
    val ownerServers: Set<ULong>,
    //var mailChannel: ULong,
    val embedColor: Int = 0x03FCC2, //HEX VALUE
    val adminId: Long,
    val serverEntries: MutableMap<Long, ServerEntry> = mutableMapOf(),
) {


    @Transient
    private val json = Json { prettyPrint = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun save() {
        File("bot.config.json").outputStream().use { output ->
            json.encodeToStream(this, output)
        }
    }

    fun getOrAddEntry(serverId: Long?) : ServerEntry? {
        if(serverId == null || serverId == 0L){
            return null
        }
        return serverEntries.getOrPut(serverId, ::ServerEntry)
    }
}

@Serializable
data class ServerEntry(
    var newsChannel: ULong? = null,
    var reportChannel: ULong? = null,
    public val roleGroups: MutableMap<String, MutableList<RoleEntry>> = mutableMapOf(), //second is the group in which the role is
)

@Serializable
data class RoleEntry(
    val roleId: ULong,
    val emoteName: String, //the full name of the emote that will be displayed in the role selector
    val fullName: String, //custom name for the role, can be different from the role's actual name
    val description: String, //the description that will be displayed in the role selector
)