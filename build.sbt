name := "summingbird-hybrid-example"

version := "1.0.0"

scalaVersion := "2.10.4"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Yresolve-term-conflict:package"
)

val bijectionVersion = "0.7.0" // (Bijection -> Conversion issue)
val configVersion = "1.2.1"
val finagleVersion = "6.22.0"
val junitVersion = "4.11"
val kafkaVersion = "0.8.1.1"
val json4sVersion = "3.2.10"
val logbackVersion = "1.1.2"
val mockitoVersion = "1.10.0"
val slf4jVersion = "1.7.7"
val scalaLoggingVersion = "2.1.2"
val scalaTestVersion = "2.2.1"
val storehausVersion = "0.9.1" 
val storehausHackVersion = "0.9.1.hack" 
val stormVersion = "0.9.2-incubating"
val summingbirdVersion = "0.5.1"
val tormentaVersion = "0.8.1" 

//"file:///home/jennifer/ws/ws2/summingbird-hybrid-example/lib/storehaus-core_2.10-0.9.1.hack.jar",

val file = (unmanagedBase / "storehaus-core_2.10-0.9.1.jar")
val storehausPath = "file://" + file.toString

dependencyOverrides += "com.twitter" %% "storehaus-core" % storehausHackVersion

libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % logbackVersion,
        "ch.qos.logback" % "logback-core" % logbackVersion,
        "org.slf4j" % "slf4j-api" % slf4jVersion,
        "com.typesafe.scala-logging" %% "scala-logging-slf4j" % scalaLoggingVersion,
        "com.typesafe" % "config" % configVersion,
        "org.json4s" %% "json4s-jackson" % json4sVersion,
        "org.json4s" %% "json4s-ext" % json4sVersion,
      	"com.twitter" %% "storehaus-core" % storehausHackVersion from storehausPath, 
        "com.twitter" %% "storehaus-memcache" % storehausVersion
            exclude("com.twitter", "storehaus-core"),
        "com.twitter" %% "storehaus-cache" % storehausVersion,
        "com.twitter" %% "storehaus-algebra" % storehausVersion
            exclude("com.twitter", "storehaus-core"),
        "com.twitter" %% "bijection-netty" % bijectionVersion,
        "com.twitter" %% "bijection-util" % bijectionVersion,
        "com.twitter" %% "finagle-memcached" % finagleVersion,
        "com.twitter" %% "summingbird-core" % summingbirdVersion,
        "com.twitter" %% "summingbird-batch" % summingbirdVersion,
        "com.twitter" %% "summingbird-online" % summingbirdVersion
            exclude("com.twitter", "storehaus-core"),
        "com.twitter" %% "summingbird-storm" % summingbirdVersion
            exclude("com.twitter", "storehaus-core"),
//      "com.twitter" %% "summingbird-scalding" % summingbirdVersion,
        "com.twitter" %% "summingbird-client" % summingbirdVersion
            exclude("com.twitter", "storehaus-core"),
        "org.apache.storm" % "storm-core" % stormVersion,
        "org.apache.storm" % "storm-kafka" % stormVersion ,
        "com.twitter" %% "tormenta-core" % tormentaVersion,
//        "com.twitter" %% "tormenta-kafka" % tormentaVersion
//            exclude("storm", "storm-kafka"),
        "org.apache.kafka" %% "kafka" % kafkaVersion 
            exclude("log4j", "log4j")
            exclude("org.slf4j", "slf4j-simple")
            exclude("javax.jms", "jms")
            exclude("com.sun.jdmk", "jmxtools")
            exclude("com.sun.jmx", "jmxri"),
        "junit" % "junit" % junitVersion % "test",
        "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
        "org.mockito" % "mockito-core" % mockitoVersion % "test"
)

//add "config folder to classpath"
unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "config"

// plugins
net.virtualvoid.sbt.graph.Plugin.graphSettings
