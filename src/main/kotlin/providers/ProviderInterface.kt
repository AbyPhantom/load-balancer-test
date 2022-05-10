package providers

interface ProviderInterface {

    var active: Boolean
    var alive: Boolean
    var heartbeatChecked: Boolean
    var capacity: Int
    fun get(): String
    fun check(): Boolean

}