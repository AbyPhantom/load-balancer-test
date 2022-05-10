package providers.implementation

import providers.ProviderInterface

class DelayedProvider(private val identifier: String) : ProviderInterface {

    override var active: Boolean = true
    override var heartbeatChecked: Boolean = false
    override var alive: Boolean = true
    override var capacity: Int = 3

    override fun get(): String {
        Thread.sleep(50)
        return this.identifier
    }

    override fun check(): Boolean  {
        return this.alive
    }

}