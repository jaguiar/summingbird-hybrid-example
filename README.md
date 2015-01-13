SummingBird hybrid example
=========

NEED TO REPAIR MANAGED LIBRARIES ISSUE

SummingBird hybrid example is a project to compute KPI from data either retrieved from a kafka topic ("online" / "streaming" mode).

Note that I am primarily a Java developer, I've been learning Scala for about 2 months, so please, Scala Masters, forgive me for my "scala-clearly-coded-by-a-stupid-java-dev" style :P

Version
----

0.0.1-SNAPSHOT

Tech
-----------

SummingBird hybrid example uses a number of open source projects to work properly:

* [Apache Zookeeper] - an open-source server which enables highly reliable distributed coordination
* [Apache Kafka] - a high-throughput distributed messaging system
* [Twitter SummingBird] - a library that lets you write MapReduce programs that look like native Scala or Java collection transformations and execute them on a number of distributed MapReduce platforms
* [Memcached] - an open-source distributed in-memory object caching system
* [Simple Building Tool] - an interactive build tool for Scala
* [Scala] - Object-Oriented Meets Functional (or so they say...)
* [Java] - huh?!

Prerequisites
--------------
* Zookeeper 3.4.5
* Kafka 0.8.1.1 (0.8.1.0 should be OK)
* Twitter Summingbird 0.5.1
* Memcached 1.4.14
* SBT 0.13.x (tested with 0.13.1/0.13.5/0.13.6). Currently set to 13.6 in project/build.properties
* Scala 10.04 - *problems have been reported with 11.x versions*
* Java 7+

Hortonworks Sandbox can be used, refer to http://hortonworks.com/hadoop/kafka/ for installing Kafka on it.

Settings
--------------
* To configure Kafka and Zookeeper, refer to [kafka-producer-example] README file
* The kafka spout (Storm) needs to have a a folder in Zookeeper to save its state. To do so:
    * Create the storm-state folder in zookeeper
```sh
    <ZOOKEPER_HOME>/bin/zkCli.sh -server <list of zookeeper nodes, e.g. localhost:2181>
    ls /
    #[zookeeper ...]
    create /yogurt.spout '' # create /<kafka spout state folder> ''
    ls /
    #[yogurt.spout, zookeeper ...]
```
* Set up your SBT environment
* Install and start memcached

Usage
--------------
####UNIX
```sh
cd summingbird-hybrid-example
sbt clean update compile
sbt -Dconfig.resource=<OBJECT>.conf "run-main <JOB_MAIN_CLASS> --local"
# e.g. run online mode only: sbt -Dconfig.resource=yogurt.conf "run-main  org.yaourtcorp.summingbird.hybrid.example.yogurt.ExeStorm --local"
# e.g. run hybrid mode: sbt -Dconfig.resource=yogurt.conf "run-main org.yaourtcorp.summingbird.hybrid.example.yogurt.RunHybrid"

After a while, you can start [kafka-producer-example] to push messages to kafka. Messages are consumed and the calculation result stored in MemCached.

To lookup result, start Scala interpreter console:
sbt -Dconfig.resource=yogurt.conf summingbird-hybrid-example/console

scala>import org.yaourtcorp.summingbird.hybrid.example.yogurt.HybridRunner._
scala>import org.yaourtcorp.summingbird.hybrid.example.yogurt.YogurtStormRunner._
scala>import org.yaourtcorp.summingbird.hybrid.example.yogurt.YogurtLookupClient._

lookup("9")

```

#####Configure the summingbird jobs
* Summingbird general configuration: summingbird-hybrid-example-*/src/main/config/application.conf (set general kafka and memecached properties like kafka host, kafka broker path, memcached host ...)
* Main program: summingbird-hybrid-example-*/src/main/config/yogurt.conf (for calculations involving yogurts, main config is to set the name of the kafka topic)
* Logs: summingbird-hybrid-example-*/src/main/config/logback.xml

Troubleshooting
--------------

Notes
--------------
You'll find some forked version jars in "lib" folder: 
1. I had to use a forked version of Tormenta which is compatible with Kafka 0.8.x, many thanks to [sdjamaa] who encounters and resolves this issue for me.
1. Because of some Scala dark magic, I encountered "java.lang.NoSuchMethodError" errors when I tried to use the latest *_2.10 versions of storehaus-core (0.9.1) with the latest (0.7.0) version of bijection. So I just recompiled storehaus-core-0.9.1 with updating the bijection dependency to 0.7.0 and put the resulting jar in lib. (Yeah, it worked, dark magic I told you! ;P)
1. Same issue and same solution as 2 for summingbird-scalding (0.5.1)  

**Free Example, Hell Yeah!**

[Apache Zookeeper]:http://zookeeper.apache.org/
[Apache Kafka]:http://kafka.apache.org/
[Java]:https://www.java.com/
[Twitter SummingBird]:https://github.com/twitter/summingbird
[Memcached]:http://memcached.org/
[Simple Building Tool]:http://www.scala-sbt.org/
[Scala]:http://www.scala-lang.org/
[kafka-producer-example]:https://github.com/jaguiar/kafka-producer-example
[sdjamaa]:https://github.com/sdjamaa/summingbird-example
