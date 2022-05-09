package provider.implementation

import provider.ProviderInterface

class DefaultProvider(private val identifier: String) : ProviderInterface {

    override fun get(): String {
        return this.identifier
    }

}