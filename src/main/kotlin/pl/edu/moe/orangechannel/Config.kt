package pl.edu.moe.orangechannel

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment

@Serializable
data class Config(val bot : Bot, val owner : Owner) {
    @Serializable
    data class Bot(@Comment("bot 账号") val account : Long, @Comment("bot 密码") val password : String)
    @Serializable
    data class Owner(@Comment("主人账号")val account : Long)
}