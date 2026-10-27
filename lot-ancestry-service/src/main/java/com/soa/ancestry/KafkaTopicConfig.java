package com.soa.ancestry;

import com.soa.common.event.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Creates the topic + dead-letter topic on startup (3 partitions, key = blendId). */
@Configuration
public class KafkaTopicConfig {
    @Bean NewTopic eventsTopic() { return TopicBuilder.name(Topics.EVENTS).partitions(3).replicas(1).build(); }
    @Bean NewTopic eventsDlt() { return TopicBuilder.name(Topics.EVENTS_DLT).partitions(3).replicas(1).build(); }
}
