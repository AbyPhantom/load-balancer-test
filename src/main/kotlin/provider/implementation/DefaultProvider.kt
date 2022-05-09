package provider.implementation

import provider.ProviderInterface

class DefaultProvider(private val identifier: String) : ProviderInterface {

    override var active: Boolean = true
    override var heartbeatChecked: Boolean = false
    override var alive: Boolean = true

    override fun get(): String {
        return this.identifier
    }

    override fun check(): Boolean  {
        return this.alive
    }
    
}