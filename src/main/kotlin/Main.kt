import exceptions.CollectionSizeException
import generators.implementation.RandomGenerator
import generators.implementation.RoundRobinGenerator
import loadbalancer.LoadBalancer
import providers.ProviderInterface
import providers.implementation.DefaultProvider
import providers.implementation.DelayedProvider
import java.util.Scanner
import java.util.stream.IntStream
import kotlin.streams.asSequence

fun main() {

    val loadBalancer : LoadBalancer = LoadBalancer(5000)

    var userInput: Int = -1
    val input = Scanner(System.`in`)

    println("Welcome to Proof of Concept tests:")
    while(userInput != 0) {
        println("Please select which test you would like to perform")
        println()
        println("1. Loading providers")
        println("2. Random invocation")
        println("3. Round Robin Invocation")
        println("4. Manual node exclusion / inclusion")
        println("5. Heart beat checker")
        println("6. Cluster Capacity Limit")
        println("0. Exit")

        userInput = input.nextInt()

        when(userInput) {
            1 -> task1(input, loadBalancer)
            2 -> task2(input, loadBalancer)
            3 -> task3(input, loadBalancer)
            4 -> task4(input, loadBalancer)
            5 -> task5(input, loadBalancer)
            6 -> task6(input, loadBalancer)
            0 -> println("Exiting...")
            else -> println("Please chose a valid option.")
        }

        println()

    }

    return

}

fun task1(input: Scanner, loadBalancer: LoadBalancer) {
    println()
    println("How many providers would you like to load?")

    val numberOfProviders: Int = input.nextInt()

    try {
        loadDefaultProviders(loadBalancer, 0, numberOfProviders)
    } catch(e: CollectionSizeException) {
        println("Tried to load $numberOfProviders providers. Failed with:")
        println("${e.message}")
        return
    }

    println("Loaded providers:")
    loadBalancer.providers.forEach{(identifier, provider) -> println("$identifier loaded")}

}

fun task2(input: Scanner, loadBalancer: LoadBalancer) {
    loadBalancer.generator = RandomGenerator()

    println()
    println("How many random invocations would you like to perform?")

    val numberOfRandomInvocations: Int = input.nextInt()

    for(i in 0 until numberOfRandomInvocations) {
        println("${loadBalancer.get()}")
    }
}

fun task3(input: Scanner, loadBalancer: LoadBalancer) {
    loadBalancer.generator = RoundRobinGenerator()

    println()
    println("How many invocations would you like to perform?")

    val numberOfInvocations: Int = input.nextInt()

    for(i in 0 until numberOfInvocations) {
        println("${loadBalancer.get()}")
    }
}

fun task4(input: Scanner, loadBalancer: LoadBalancer) {
    println()
    println("Type in include or exclude followed by provider name to include/exclude it")
    println("For example:")
    println("exclude provider_1")
    println("After you are done, you can test it with round robin invocation")

    val inputString: String = readln()
    val command = inputString.split(' ')[0]
    val providerName = inputString.split(' ')[1]


    when(command) {
        "include" -> {
            loadBalancer.include(providerName)
            println("$providerName included")
        }
        "exclude" -> {
            loadBalancer.exclude(providerName)
            println("$providerName excluded")
        }
    }
}

fun task5(input: Scanner, loadBalancer: LoadBalancer) {
    println()
    println("Preparing the providers for a test.")
    println("5 providers will be excluded, with 2 of them being alive")
    println("5 providers will be included, with 2 of them being alive")
    println("Once the test starts if will last for 15 seconds. Hearbeat is checked every 5 seconds.")

    loadDefaultProviders(loadBalancer, 0, 10)

    IntStream.range(0, 5).forEach { loadBalancer.providers.values.elementAt(it).active = false }
    IntStream.range(0, 3).forEach { loadBalancer.providers.values.elementAt(it).alive = false }
    IntStream.range(3, 5).forEach { loadBalancer.providers.values.elementAt(it).alive = true }

    IntStream.range(5, 10).forEach { loadBalancer.providers.values.elementAt(it).active = true }
    IntStream.range(5, 8).forEach { loadBalancer.providers.values.elementAt(it).alive = false }
    IntStream.range(8, 10).forEach { loadBalancer.providers.values.elementAt(it).alive = true }

    loadBalancer.startHeartbeat()
    Thread.sleep(15 * 1000)
    loadBalancer.stopHeartbeat()
}

fun task6(input: Scanner, loadBalancer: LoadBalancer) {
    loadDelayedProviders(loadBalancer, 0, 10)
    println()
    println("Testing the cluster capacity limit")
    println("This test uses DelayedProviders which simulate network delay in response")
    println("How many requests would you like to send?")

    val numberOfRequests: Int = input.nextInt()

    for(i in 0 until numberOfRequests) {
        loadBalancer.request()
    }
}

fun loadDefaultProviders(loadBalancer: LoadBalancer, rangeStart: Int, rangeFinish: Int) {
    val namePrefix : String = "provider_"

    val providers : Map<String, ProviderInterface> = IntStream.range(rangeStart, rangeFinish).asSequence()
        .map { Pair(namePrefix+it, DefaultProvider(namePrefix+it)) }
        .toMap<String, ProviderInterface>();

    loadBalancer.register(providers)
}

fun loadDelayedProviders(loadBalancer: LoadBalancer, rangeStart: Int, rangeFinish: Int) {
    val namePrefix : String = "provider_"

    val providers : Map<String, ProviderInterface> = IntStream.range(rangeStart, rangeFinish).asSequence()
        .map { Pair(namePrefix+it, DelayedProvider(namePrefix+it)) }
        .toMap<String, ProviderInterface>();

    loadBalancer.register(providers)
}