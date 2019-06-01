package org.revo.fm.codec;


import org.revo.fm.codec.aac.AdtsFrame;
import org.revo.fm.codec.rtp.RtpPkt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RtpPktToAdtsFrame implements Function<RtpPkt, List<AdtsFrame>> {
    @Override
    public List<AdtsFrame> apply(RtpPkt rtpPkt) {
        List<AdtsFrame> data = new ArrayList<>();
        int auHeaderLength = StaticProcs.bytesToUIntInt(rtpPkt.getPayload(), 0) >> 3;
        int offset = 2 + auHeaderLength;
        for (int i = 0; i < (auHeaderLength / 2); i++) {
            int size = StaticProcs.bytesToUIntInt(rtpPkt.getPayload(), 2 + (i * 2)) >> 3;
            data.add(new AdtsFrame("1", rtpPkt.getPayload(), offset, size));
            offset += size;
        }

        return data;
    }
}
