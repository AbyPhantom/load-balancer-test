package generators.implementation

import generators.ProviderGenerator

class RoundRobinGenerator : ProviderGenerator() {

    var currentValue: Int = -1

    override fun next(size: Int): Int {

        if(this.currentValue + 1 >= size) {
            this.currentValue = -1
        }

        this.currentValue += 1

        return currentValue

    }
}