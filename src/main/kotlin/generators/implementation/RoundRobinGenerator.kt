package generators.implementation

import generators.ProviderGenerator

/**
 * RoundRobin generator returns the index according to the RoundRobin rules.
 * It will increase by one on each subsequent call until it reaches the end of the size,
 * at which point it will loop back to the beginning.
 */
class RoundRobinGenerator : ProviderGenerator() {

    private var currentValue: Int = -1

    override fun next(size: Int): Int {

        if(this.currentValue + 1 >= size) {
            this.currentValue = -1
        }

        this.currentValue += 1

        return currentValue

    }
}