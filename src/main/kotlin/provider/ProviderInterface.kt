package provider

interface ProviderInterface {

    var active: Boolean
    var alive: Boolean
    var heartbeatChecked: Boolean
    fun get(): String
    fun check(): Boolean

}