/*============================================================================
 * kafka-streams-test
 * App.java
 * =========================================================================*/
package me.kevinboone.apacheintegration.kafka_streams_test;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** App is the main (and only) class in the application. It has one
 *    method, buildTopology(), that is annotated with Produces. The
 *    Quarkus framework recognizes that the return value, of type
 *    Topology, is something that is meaningful to the Kafka Streams
 *    extension, and instantiates the Streams framework. */
@ApplicationScoped
public class App
  {
  @ConfigProperty(name = "test.input-topic")
  String inputTopic;

  @ConfigProperty(name = "test.output-topic")
  String outputTopic;

  @Produces
  public Topology buildTopology() 
    { 
    // This is almost boilerplate Kafka Streams code: read from
    //   a topic, write to a topic, with a small change in between.
    StreamsBuilder builder = new StreamsBuilder();
    builder.stream (inputTopic, 
           Consumed.with (Serdes.String(), Serdes.String()))
        .peek((k,v) -> System.out.println ("in=" + v))
        .mapValues ((v) -> v.toLowerCase())
        .peek((k,v) -> System.out.println ("out=" + v))
        .to (outputTopic, Produced.with (Serdes.String(), Serdes.String()));
    return builder.build();
    }
  }


