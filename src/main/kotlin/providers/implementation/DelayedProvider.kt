package providers.implementation

import providers.ProviderInterface

/**
 * Provider implementation used to simulate network delays and provider workload.
 *
 * Get method simulates the delay by putting the thread to sleep for 1 second.
 */
class DelayedProvider(private val identifier: String) : ProviderInterface {

    override var active: Boolean = true
    override var heartbeatChecked: Boolean = false
    override var alive: Boolean = true
    override var capacity: Int = 3

    /**
     * Get method simulates the delay by putting the thread to sleep for 1 second.
     */
    override fun get(): String {
        Thread.sleep(1000)
        return this.identifier
    }

    override fun check(): Boolean  {
        return this.alive
    }

}