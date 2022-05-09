package loadbalancer

import exceptions.CollectionSizeException
import provider.ProviderInterface

class LoadBalancer {

    private val capacity: Int = 10

    var providers: Set<ProviderInterface> = LinkedHashSet<ProviderInterface>()
        private set

    fun register(providers: Set<ProviderInterface>) {
        if(providers.size > this.capacity) {
            throw CollectionSizeException("Maximum size of providers is ${this.capacity}")
        }
        this.providers = providers
    }

}