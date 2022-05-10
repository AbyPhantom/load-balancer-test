# Load Balancer Test

## Definition

A load balancer is a component that, once invoked, it distributes incoming requests to a list of registered providers and return the value obtained from one of the registered providers to the original caller. For simplicity we will consider both the load balancer and the provider having a public method named get().

![load_balancer](https://user-images.githubusercontent.com/5208793/167361099-33e8191e-c2b2-416b-bd4b-5b1185e793a6.png)

## Requirements

1. Generate a provider
    - Generate a Provider that, once invoked on his get() method, retrieve an unique identifier (string) of the provider instance
2. Register a list of providers
    - Register a list of provider instances to the Load Balancer - the maximum number of providers accepted from the load balancer is 10
3. Random invocation
    - Develop an algorithm that, when invoking multiple times the Load Balancer on its get() method, should cause the random invocation of the get() method of any registered provider instance.
4. Round Robin invocation
    - Develop an algorithm that, when invoking multiple times the Load Balancer on its get() method, should cause the round-robin (sequential) invocation of the get() method of the registered providers.
5. Manual node exclusion / inclusion
    - Develop the possibility to exclude / include a specific provider into the balancer
6. Heart beat checker
    - The load balancer should invoke every X seconds each of its registered providers on a special method called check() to discover if they are alive. If not, it should exclude the provider node from load balancing.
7. Improving Heart beat checker
    - If a node has been previously excluded from the balancing it should be re-included if it has successfully been “heartbeat checked” for 2 consecutive times
8. Cluster Capacity Limit
    - Assuming that each provider can handle a maximum number of Y parallel requests, the Balancer should not accept any further request when it has (Y*aliveproviders) incoming requests running simultaneously


## Usage

Compile and run the code. You will be presented with an interactive console guide that will guide you with testing various functionalities.

### 1. Loading providers
Simply enter the number of providers you want to load. The providers loaded will be shown in the output. Naming convention is provider_0, provider_1 etc.

### 2. Random invocation
After loading the providers you can enter the number of get request calls. In the output, you will see which provider got called.

### 3. Round Robin invocation
After loading the providers you can enter the number of get request calls. In the output, you will see which provider got called.

### 4. Manual node inclusion/exclusion
Use the following syntax to inlclude or exclude the node
```
include provider_1
exclude provider_2
```
You can use Round Robin invocation afterwards to verify that the providers will/won't get called accordingly.

### 5. Heart beat checker
This test will automatically load a configuration of providers in order to test the functionality.
There will be 10 providers in total, as follows:
- 3 that are excluded and not alive. Their status won't change during the test
- 2 that are excluded, but alive. They will become included after 2 heartbeats
- 3 that are included, but not alive. They will become excluded on the first heartbeat
- 2 that are included and alive. Their status won't change during the test
**Note** This test lasts for 15 seconds, during which you need to just wait and see the results

### 6. Cluster capacity limit
In this test you can test sending various amount of requests to providers. They are executed asynchronously and the providers simulate a network delay (1 second). If you send appropriate amount of requests, i.e. amount of providers * 3 (3 is the artificial limit of "simultanious requests" on the providers), you will see the responses. However, sending more than can be handled, i.e. 100, will return most of them as rejected.
**Note** since this test is heavily delayed, "main menu" will show up again and wait for your input. It is best not to type in anything until the test is done.


## Structure

### 1. Main
A script that shows interactive program described above.

### 2. LoadBalancer
LoadBalancer is the main class that this project revolves around. It is used to simulate a real life load balancer in a local environment.

When instancing it, it takes one parameter - check period, which is a number of miliseconds for which the heartbeat check will occur.

#### Properties
It can load up to 10 providers (ProviderInterface). This is set in capacity property.

Hearbeat implementation is a thread that loops until a flag checkForHeartbeat is true (this is used in case we want to turn off the heartbeat monitor). It will call the check method explained later.

LoadBalancer can be configured with a ProviderGenerator. The purpose of the generator is to pick the next provider when calling get() method multiple times. In this project RandomGenerator and RoundRobinGenerator are provided.

Providers property is a HashMap that holds String identifiers and the Providers themselves. Initially this was a set, but later I realized that we should not be dependent on get() method to get the identifier (for logging purposes, for example), because the providers might not be alive.

Request and response queues are blocking queues used to coordinate request and response runnables.

Request dispatcher coordinates all the activities of the aforementioned threads (explained in detail in it's own section)

#### Methods
Register takes a Map of Providers and assigns it to the property of the same name. It also initializes request capacity, request/response queues and initializes and starts request dispatcher.

Get method returns the example response of a provider. In this use case it is a unique identifier of the provider (name). It uses ProviderGenerator to pick which provider to call.

Request is a more advanced method that uses request dispatcher to submit a request to a thread that will handle it.

Include/exclude mark the provider as active/inactive so it would or wouldn't be used in operations.

Check is a method for heartbeat checking. It checks all the providers and includes/excludes them as per following rules:
1. If a provider is not alive, it excludes it
2. If a provider is alive, but excluded it will mark it so that it gets included on the next healtcheck
3. It includes previously marked providers that are alive, but excluded

GetNextIdentifier is an internal helper method used to get the identifier of the next provider to be used. It relies on the ProviderGenerator implementation.

GetRequestCapacity is an internal helper method that sums up the capacity of all the providers. It is used instead of capacity * number of providers formula in case that providers have different capacities.

Start/Stop Heartbeat starts/stops the heartbeat thread.

Shutdown stops the heartbeat and stops the request dispatcher (with its executor and thread pool)

### Request Dispatcher

Object of this class manages the threads used in request/response cycle. It creates a fixed thread pool and adds the request and response threads to it per incoming request.

Submit request method will check if request queue is full and if not, add a new request to it. Otherwise it adds appropriate response to response queue.

### Request Runnable

A runnable implementation that handles the request from request queue and puts the response in response queue.

### Response Runnable

A runnable implementation that takes the response from response queue and prints it to console output.



