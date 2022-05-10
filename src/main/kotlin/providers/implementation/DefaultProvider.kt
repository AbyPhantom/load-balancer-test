package providers.implementation

import providers.ProviderInterface

class DefaultProvider(private val identifier: String) : ProviderInterface {

    override var active: Boolean = true
    override var heartbeatChecked: Boolean = false
    override var alive: Boolean = true
    override var capacity: Int = 3

    override fun get(): String {
        return this.identifier
    }

    override fun check(): Boolean  {
        return this.alive
    }
    
}