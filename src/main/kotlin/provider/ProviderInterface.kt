package provider

interface ProviderInterface {

    var active: Boolean
    var alive: Boolean
    fun get(): String
    fun check(): Boolean {
        return this.alive
    }

}