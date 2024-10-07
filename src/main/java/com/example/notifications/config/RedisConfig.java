package com.example.notifications.config;


import com.example.notifications.data.redis.RedisNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, RedisNotification> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RedisNotification> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  //JavaTimeModule is registered for Instant and other date/time types
        objectMapper.findAndRegisterModules();

        Jackson2JsonRedisSerializer<RedisNotification> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, RedisNotification.class);

        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(serializer);

        return template;
    }
}