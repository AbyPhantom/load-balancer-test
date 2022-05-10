package dispatcher

import providers.ProviderInterface

class Request(val command: Command, val provider: ProviderInterface) {
    enum class Command {
        GET
    }
}