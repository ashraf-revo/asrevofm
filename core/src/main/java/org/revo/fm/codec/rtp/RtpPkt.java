/**
 * Java RTP Library (jlibrtp)
 * Copyright (C) 2006 Arne Kepp
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.revo.fm.codec.rtp;


import org.revo.fm.codec.StaticProcs;

public class RtpPkt {
    private int version = 2;        //2 bits
    private int padding;            //1 bit
    private int extension = 0;        //1 bit
    private int marker = 0;            //1 bit
    private int payloadType;        //
    private int seqNumber;            //16 bits
    private long timeStamp;            //32 bits
    private long ssrc;                //32 bits
    private long[] csrcArray = null;//
    private byte[] rawPkt = null;
    private byte[] payload = null;
    private int rtpChannle;

    public RtpPkt() {
    }

    public RtpPkt(int rtpChannle, byte[] aRawPkt, int packetSize) {
        this.rtpChannle = rtpChannle;
        if (aRawPkt == null) {
            System.out.println("RtpPkt(byte[]) Packet null");
        }

        int remOct = packetSize - 12;
        if (remOct >= 0) {
            rawPkt = aRawPkt;    //Store it
            sliceFirstLine();
            if (version == 2) {
                sliceTimeStamp();
                sliceSSRC();
                if (remOct > 4 && getCsrcCount() > 0) {
                    sliceCSRCs();
                    remOct -= csrcArray.length * 4; //4 octets per CSRC
                }
                // TODO Extension
                if (remOct > 0) {
                    slicePayload(remOct);
                }

                //Sanity checks

                //Mark the buffer as current
            } else {
                System.out.println("RtpPkt(byte[]) Packet is not version 2, giving up.");
            }
        } else {
            System.out.println("RtpPkt(byte[]) Packet too small to be sliced");
        }
    }

    public RtpPkt(byte[] payload) {
        this(1, payload, payload.length);
    }


    public int getRtpHeaderLength() {
        //TODO include extension
        return 12 + 4 * getCsrcCount();
    }

    private int getPayloadLength() {
        return payload.length;
    }


    protected int getVersion() {
        return version;
    }


    public boolean isMarked() {
        return (marker != 0);
    }

    public int getPayloadType() {
        return payloadType;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getSsrc() {
        return ssrc;
    }

    public byte[] getRawPkt() {
        return rawPkt;
    }

    public int getCsrcCount() {
        if (csrcArray != null) {
            return csrcArray.length;
        } else {
            return 0;
        }
    }

    public long[] getCsrcArray() {
        return csrcArray;
    }

    public int getRtpChannle() {
        return rtpChannle;
    }


    public byte[] getPayload() {
        return payload;
    }

    private void sliceFirstLine() {
        version = ((rawPkt[0] & 0xC0) >>> 6);
        padding = ((rawPkt[0] & 0x20) >>> 5);
        extension = ((rawPkt[0] & 0x10) >>> 4);
        csrcArray = new long[(rawPkt[0] & 0x0F)];
        marker = ((rawPkt[1] & 0x80) >> 7);
        payloadType = (rawPkt[1] & 0x7F);
        seqNumber = StaticProcs.bytesToUIntInt(rawPkt, 2);
    }

    private void sliceTimeStamp() {
        timeStamp = StaticProcs.bytesToUIntLong(rawPkt, 4);
    }

    private void sliceSSRC() {
        ssrc = StaticProcs.bytesToUIntLong(rawPkt, 8);
    }

    private void sliceCSRCs() {
        for (int i = 0; i < csrcArray.length; i++) {
            ssrc = StaticProcs.bytesToUIntLong(rawPkt, i * 4 + 12);
        }
    }

    private void slicePayload(int bytes) {
        payload = new byte[bytes];
        int headerLen = getRtpHeaderLength();

        System.arraycopy(rawPkt, headerLen, payload, 0, bytes);
    }
}	