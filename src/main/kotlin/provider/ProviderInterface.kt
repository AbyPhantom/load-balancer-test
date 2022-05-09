package provider

interface ProviderInterface {
    fun get(): String

    var active: Boolean

}