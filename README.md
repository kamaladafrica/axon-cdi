# Axon Framework CDI Integration

A CDI Extension that helps [Axon Framework](http://www.axonframework.org) and CDI integration.
You only need to @Produces some required instances and annotate them with @Autoconfigure, in order to get Axon Framework up and running in a CDI environment.

### What you have to do

* Write your command handlers, event handlers, sagas and aggregate roots.
* Write producer methods for CommandBus, EventBus, EventStore and SagaRepository.
* Annotate producer methods whith @AutoConfigure
* Optionally you can write producer for Snapshotter and SnapshotterTrigger

### What you get
* An instance of EventSourcingRepository for every aggregate root
* An instance of SagaManager
* Every command handler registered with command bus
* Every event listenerr registered with event bus
* Every saga registered with saga manager
* If you don't provide a SagaFactory, a CdiSagaFactory will be used
* If a SnapshotterTrigger is provided, it will be registered with repositories
* If a Snapshotter is provided, aggregate factories will be registered with snapshotter

## Usage

You can checkout the [Quickstart project](https://github.com/kamaladafrica/axon-cdi-quickstart) for a full working example.

### Simple usage
Annotate your command bus, event bus and snapshotter producers with `@AutoConfigure`. Aggregate roots and sagas don't need to be annotated.
As result you get:
* An AggregateRepository for every aggregate root created, configured with EventStore, SnapshotterTrigger, ConflictResolver and registered with event bus 
* Aggregate roots registered with command bus
* Event handlers registered with event bus
* Command handlers registered with command bus
* Aggregate factories registered with snapshotter
* SagaManager created, configured with AnnotatedSaga and registered with event bus
* A CDI capable SagaFactory created

### Advanced usage

There are available two annotations that allow you to customize configuration: `@AggregateConfiguration` and `@SagaConfiguration`.
Both work thanks to memes, but...

#### ...what is a "**meme**"?

A **meme** is a type (interface type is recommended, but it can be a class) which is annotated with cdi qualifiers. 
The "meme" concept is introducted to bypass some limitation of annotation declarations.
For example, if we have the qualifier @MyAwesomeQualifier, a meme can be decalred as
```java
@MyAwesomeQualifier
public class interface MyAwesomeQualifierMeme {}
```
Meme allows CDI extension to capture the exact qualifier instances.
Memes can declare qualifiers as complex as you need
```java
@MyAwesomeQualifier @MyWonderfulQualifier(value="Yes! I'm the best") 
public class interface MyAwesomeQualifierMeme {}
```

#### Aggregate root configuration

Suppose you have in your application a single command bus, but two event bus, one to dispatch and store events in a database for the aggregate A, and one to store events in a different database for the aggregate B.
Then we need at least a qualifier annotation in order to distinguish the event bus configurations.
How do you tell Axon that aggregate A should use event bus A and aggregate B the event bus B? By configuring axon-cdi as follow:

CDIConfiguration.java
```java
	@Produces
	@AutoConfigure
	@ApplicationScoped
	public CommandBus commandBus() {...}

	@Produces
	@AutoConfigure
	@ApplicationScoped
	public EventBus eventBusAggregateA() {...}

	@Produces
	@AutoConfigure
	@AggregateBQualifier
	@ApplicationScoped
	public EventBus eventBusAggregateB() {...}

	@Produces
	@ApplicationScoped
	public EventStore eventStoreAggregateA() {...}

	@Produces
	@AggregateBQualifier
	@ApplicationScoped
	public EventStore eventStoreAggregateB() {...}

```
AggregateBQualifierMeme.java
```java
@AggregateBQualifier
public interface AggregateBQualifierMeme {}
```
AggregateA.java
```java
public class AggregateA extends AbstractAnnotatedAggregateRoot<String>
```
AggregateB.java
```java
@AggregateConfiguration(
		value = AggregateBQualifierMeme.class, 
		commandBus = DefaultQualifierMeme.class, 
		snapshotter = DefaultQualifierMeme.class, 
		snapshotterTrigger = DefaultQualifierMeme.class
)
public class AggregateB extends AbstractAnnotatedAggregateRoot<String>
```

In this way you tell axon-cdi to register the AggregateB with:
* Event bus qualified with @AggregateBQualifier
* EventStore qualified with @AggregateBQualifier
* ConflictResolver qualified with @AggregateBQualifier
* CommandBus with no qualifier
* Snapshotter with no qualifier
* SnapshotterTrigger with no qualifier

*NB. CDI specification states that beans with no qualifiers are implicitly qualified with @Default (in addition to @Any)*

In order to make code cleaner, stereotypes are supported so you can annotate you aggregate root with @AggregateBStereotype declared as follow
```java
@AggregateConfiguration(
		value = AggregateBQualifierMeme.class, 
		commandBus = DefaultQualifierMeme.class, 
		snapshotter = DefaultQualifierMeme.class, 
		snapshotterTrigger = DefaultQualifierMeme.class
)
@Stereotype
@Retention(RUNTIME)
@Target(TYPE)
public @interface AggregateBStereotype {
```
and your AggregateB
```java
@AggregateBStereotype
public class AggregateB extends AbstractAnnotatedAggregateRoot<String>
```
<!---
## Getting started

All you need to do is to add the [Jitpack.io](https://jitpack.io)  repository in the maven pom.xml


		<repository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
			<name>Jitpack.io Repository</name>
			<releases>
				<enabled>true</enabled>
			</releases>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>

and declare the dependency

		<dependency>
		  <groupId>com.github.kamaladafrica</groupId>
		  <artifactId>axon-cdi</artifactId>
		  <version>${version}</version>
		</dependency>

For the very last version you can write `<version>master</version>`
 --->

## Contributing

Here are some ways for you to contribute:

* Create [GitHub tickets](https://github.com/kamaladafrica/axon-cdi/issues) for bugs or new features and comment on the ones that you are interested in.
* GitHub is for social coding: if you want to write code, we encourage contributions [through pull requests](https://help.github.com/articles/creating-a-pull-request)
  from [forks of this repository](https://help.github.com/articles/fork-a-repo).
  If you want to contribute code this way, please reference a GitHub ticket as well covering the specific issue you are addressing.
