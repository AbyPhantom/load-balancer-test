import exceptions.CollectionSizeException
import iterators.implementation.RoundRobinGenerator
import loadbalancer.LoadBalancer
import provider.ProviderInterface
import provider.implementation.DefaultProvider
import java.util.stream.IntStream
import kotlin.streams.asSequence

fun main() {

    val loadBalancer : LoadBalancer = LoadBalancer(5000)

    try {
        loadProviders(loadBalancer, 0, 12)
    } catch(e: CollectionSizeException) {
        println("Tried to load 12 providers. Failed with:")
        println("${e.message}")
    }

    println()

    loadProviders(loadBalancer, 0, 10)

    println("Load balancer provider count: ${loadBalancer.providers.size}")
    loadBalancer.providers.forEach { (_, provider) -> println("Provider ${provider.get()} loaded.") }

    println()

    println("Getting random provider identifiers (10 iterations):")
    IntStream.range(0, 10)
        .forEach { _ ->
            println("${loadBalancer.get()}")
        }

    println()

    loadBalancer.generator = RoundRobinGenerator()
    println("Getting round robin provider identifiers (20 iterations):")
    IntStream.range(0, 20).forEach { _ ->
        println("${loadBalancer.get()}")
    }

    println()

    println("Round Robin with 5 providers excluded")
    IntStream.range(0, 5).forEach { loadBalancer.exclude("provider_$it") }
    IntStream.range(0, 20).forEach { _ ->
        println("${loadBalancer.get()}")
    }

    println()

    println("Testing heartbeat check:")
    IntStream.range(0, 5).forEach { loadBalancer.providers.values.elementAt(it).active = false }
    IntStream.range(0, 3).forEach { loadBalancer.providers.values.elementAt(it).alive = false }
    IntStream.range(3, 5).forEach { loadBalancer.providers.values.elementAt(it).alive = true }

    IntStream.range(5, 10).forEach { loadBalancer.providers.values.elementAt(it).active = true }
    IntStream.range(5, 8).forEach { loadBalancer.providers.values.elementAt(it).alive = false }
    IntStream.range(8, 10).forEach { loadBalancer.providers.values.elementAt(it).alive = true }

    loadBalancer.startHeartbeat()
    Thread.sleep(20 * 1000)

}

fun loadProviders(loadBalancer: LoadBalancer, rangeStart: Int, rangeFinish: Int) {
    val namePrefix : String = "provider_"

    val providers : Map<String, ProviderInterface> = IntStream.range(rangeStart, rangeFinish).asSequence()
        .map { Pair(namePrefix+it, DefaultProvider(namePrefix+it)) }
        .toMap<String, ProviderInterface>();

    loadBalancer.register(providers)
}