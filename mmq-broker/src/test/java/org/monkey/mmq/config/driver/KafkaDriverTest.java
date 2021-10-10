package org.monkey.mmq.config.driver;

import io.netty.handler.codec.string.StringEncoder;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * @ClassNameKafkaDriverTest
 * @Description
 * @Author Solley
 * @Date2021/10/10 14:46
 * @Version V1.0
 **/
public class KafkaDriverTest {
    @Test
    public void testKafka() throws InterruptedException {
        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.154.20.191:9093,10.154.20.192:9093,10.154.20.193:9093");
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        Producer<String, String> producer = new KafkaProducer<>(prop);
        int i = 0;
        for (i = 0; i < 100; i++) {
            Future<RecordMetadata> recordMetadataFuture = producer.send(new ProducerRecord<String, String>("test", "msg:" + i++));
            Thread.sleep(1000);
        }

    }

}