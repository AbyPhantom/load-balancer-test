package provider.implementation

import provider.ProviderInterface

class DefaultProvider(private val identifier: String) : ProviderInterface {

    override var active: Boolean = true
    override var alive: Boolean = true

    override fun get(): String {
        return this.identifier
    }
    
}