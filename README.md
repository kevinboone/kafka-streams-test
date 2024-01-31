# kafka-streams-test

v1.0.0

This is a trivial application that uses the Kafka Streams extension for
Quarkus. All it does is read data from one Kafka topic, which is assumed to be
textual, and write its lower-case equivalent to another topic. All the logic,
such as it is, is in the single Java source `App.java`. 

This is just about the simplest Kafka Streams application I could think of. Of
course, the Kafka Streams logic can be expanded as required.

The application can be run on a local workstation, or be deployed to
OpenShift/Kubernetes.  It can run in conventional Java JIT mode, or
pre-compiled to native code (using GraalVM).

## Set-up

You'll need a working installation of Kafka, Maven to build the source, and JDK
11 or later. If you want to build a native code executable you'll need a
reasonably up-to-date installation of GraalVM, or Docker to run the build in a
container.

You'll probably need to change the Kafka bootstrap URL in
`application.properties`, or override it in some way.  There might be other
Kakfa-related settings to change if, for example, you're using authentication.

Kafka Streams requires its input topics to exist in advance, and will wait for
them to be created.  Use, for example, `kafka-topics.sh --create` to create the
topics that will be used for input and output. Change these topic names in
`application.properties` if necessary.

## Building

To build and run locally in development mode:

   mvn compile quarkus:dev

To run locally with a Kafka bootstrap URL that is different to the one
in `application.properties`:

    QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS=localhost:9092 mvn quarkus:dev

To build a self-contained uber-JAR (a.k.a 'fat JAR'):

    mvn package -Dquarkus.package.type=uber-jar

To compile to native code (and this does work for me, at least on Linux)

    GRAALVM_HOME=/path/to/graalvm ./mvnw package -Dnative

If you don't have GraalVM you could try building in a container
(requires Docker):

    mvn package -Dnative -Dquarkus.native.container-build=true

Note that the compiled code will be about 90Mb, which is a lot -- but not as
large as a JVM. Start-up time for the compiled version is astonishingly quicker
than for the conventional Java version, but I haven't found run-time speed to
be any different.

To deploy on OpenShift/Kubernetes:

Log in to the cluster and select the desired target namespace. Then
        
    mvn clean compile package -Dquarkus.kubernetes.deploy=true

The deployment process creates a builder pod, then a deployment pod, 
which then creates the application pod. This can take a few minutes.

## Testing

With the application running, post some strings to the topic `foo`
(unless you've changed this), and read from topic `bar`. You should
see the values changed to all lowercase. The original and modified
versions are also written to stdout.

## Health check

The SmallRye health check is enabled, which includes information from
Kafka Streams. To invoke it use, for example:

    curl localhost:8080/q/health/

This is what the automatically-generated Kubernetes readiness and liveness
probes do.

## Notes

`pom.xml` references the latest (at the time of writing) community Quarkus
BOM. I've tested with the latest Red Hat Quarkus BOM as well. 

When deploying on OpenShift/Kubernetes, the Quarkus build process generates
liveness and readiness probes that are linked to the SmallRye health
check. The application pod will not start fully until the health checks
pass, which checks include that the source topic exists. So if the
topic does not exist, you can expect to find the pod stuck in the
CrashLoopBackoff state, with a message like this in the log:

~~~
2024-01-31 09:25:16,101 INFO  [io.sma.health] (executor-thread-1) SRHCK01001: 
Reporting health down status: {"status":"DOWN","checks":[{"name":"Kafka Streams
topics health check","status":"DOWN","data":{"missing_topics":"foo"}}]}
~~~

To test on OpenShift/Kubernetes, you can log into one of the Kafka broker pods
and use `./bin/kakfa-console-producer.sh` to post messages to the
source topic, and `./bin/kafka-console-consumer.sh` to read the results.
Also, you can look at the pod logs to see the input and output.



