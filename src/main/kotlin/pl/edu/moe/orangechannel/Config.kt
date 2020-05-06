package pl.edu.moe.orangechannel

import kotlinx.serialization.Serializable

@Serializable
data class Config(val bot : Bot, val owner : Owner) {
    @Serializable
    data class Bot(val account : Long, val password : String)
    @Serializable
    data class Owner(val account : Long)
}