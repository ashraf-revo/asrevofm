package org.revo.pipeline;

import org.revo.fm.codec.rtp.RtpPkt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;

@SpringBootApplication
@EnableBinding({Source.class})
public class PipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(PipelineApplication.class, args);
    }

    @Bean
    public IntegrationFlow processUniCastUdpMessage(Source source) {
        return IntegrationFlows
                .from(new UnicastReceivingChannelAdapter(11111))
                .<byte[], RtpPkt>transform(RtpPkt::new)
                .channel(source.output())
                .get();
    }
}
