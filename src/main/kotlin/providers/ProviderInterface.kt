package providers

/**
 * Interface for Provider implementations
 */
interface ProviderInterface {

    /**
     * Active status of the provider.
     *
     * If a provider is active, it will get used for get and request calls, otherwise it will not
     */
    var active: Boolean

    /**
     * Alive status of the provider.
     *
     * This simulates the reachability of the provider. Providers with the alive status set to false will be excluded
     * by the heartbeat monitor
     */
    var alive: Boolean

    /**
     * Used by the heartbeat monitor in order to include the excluded, but alive providers after 2 times they
     * have been scanned as alive.
     */
    var heartbeatChecked: Boolean

    /**
     * Capacity for provider's requests.
     */
    var capacity: Int

    /**
     * Simulation of the GET method of the provider.
     */
    fun get(): String

    /**
     * Simulation of the heartbeat check for the provider.
     */
    fun check(): Boolean

}