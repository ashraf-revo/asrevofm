package org.revo.asrevocast;

import org.revo.fm.codec.RtpPktToAdtsFrame;
import org.revo.fm.codec.aac.AdtsFrame;
import org.revo.fm.codec.rtp.RtpPkt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.Message;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
@EnableBinding({Sink.class})
public class AsrevocastApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsrevocastApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> function(Flux<AdtsFrame> rtpPktsFlux) {
        return route(GET("/aac/{ch}"), serverRequest -> ok()
                .header("Content-Type", "audio/aac")
                .body(rtpPktsFlux
                        .filter(it -> it.channel.equals(serverRequest.pathVariable("ch")))
                        .map(it -> new DefaultDataBufferFactory().wrap(it.getRaw())), DataBuffer.class));
    }

    @Bean
    public FluxProcessor<RtpPkt, RtpPkt> rtpSource() {
        return ReplayProcessor.create(0);

    }

    @Bean
    public Flux<AdtsFrame> rtpPktsFlux(FluxProcessor<RtpPkt, RtpPkt> rtpPkts) {
        RtpPktToAdtsFrame rtpPktToAdtsFrame = new RtpPktToAdtsFrame();
        return rtpPkts.publish().autoConnect()
                .flatMap(it -> Mono.just(rtpPktToAdtsFrame.apply(it)).flatMapMany(Flux::fromIterable));
    }

    @Autowired
    private FluxProcessor<RtpPkt, RtpPkt> rtpPkts;

    @StreamListener(Sink.INPUT)
    public void new_video(Message<byte[]> event) {
        rtpPkts.onNext(new RtpPkt(event.getPayload()));
    }
}
