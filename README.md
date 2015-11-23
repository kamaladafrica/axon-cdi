# Axon Framework CDI Integration

A CDI Extension that helps [Axon Framework](http://www.axonframework.org) and CDI integration.
You only need to @Produces some required instances and annotate them with @Autoconfigure, in order to get Axon Framework up and running in a CDI environment.

### What you have to do

* Write your command handlers, event handlers, sagas and aggregate roots.
* Write producer methods for CommandBus, EventBus, EventStore and SagaRepository.
* Annotate producer methods whith @AutoConfigure
* Optionally you can write producer for Snapshotter and SnapshotterTrigger
* Optionally you can write producer for TransactionManager

### What you get
* An instance of EventSourcingRepository for every aggregate root
* An instance of SagaManager
* Every command handler registered with command bus
* Every event listenerr registered with event bus
* Every saga registered with saga manager
* If you don't provide a SagaFactory, a GenericSagaFactory configured with a configured CDI resource injector will be used
* If a SnapshotterTrigger is provided, it will be registered with repositories
* If a Snapshotter is provided, it will be configured
* If a TransactionManager is provided, it is used to configure the Snaphotter

## Usage

You can checkout the [Quickstart project](https://github.com/kamaladafrica/axon-cdi-quickstart) for a full working example.


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
 

## Contributing

Here are some ways for you to contribute:

* Create [GitHub tickets](https://github.com/kamaladafrica/axon-cdi/issues) for bugs or new features and comment on the ones that you are interested in.
* GitHub is for social coding: if you want to write code, we encourage contributions [through pull requests](https://help.github.com/articles/creating-a-pull-request)
  from [forks of this repository](https://help.github.com/articles/fork-a-repo).
  If you want to contribute code this way, please reference a GitHub ticket as well covering the specific issue you are addressing.
