package org.revo.fm.codec.aac;

public class AdtsFrame {
    public String channel;
    private byte[] raw;

    public AdtsFrame(String channel, byte[] payload, int offset, int size) {
        this.channel = channel;
        byte[] adtsHeader = getAdtsHeader(size);
        raw = new byte[size + adtsHeader.length];
        System.arraycopy(adtsHeader, 0, raw, 0, adtsHeader.length);
        System.arraycopy(payload, offset, raw, adtsHeader.length, size);
    }


    public byte[] getRaw() {
        return raw;
    }

    private static byte[] getAdtsHeader(int size) {
        byte[] adtsHeader = new byte[]{(byte) 0xFF, (byte) 0xF9, (byte) 0x4C, (byte) 0x80, (byte) 0x2F, (byte) 0x5F, (byte) 0xFD};
        size += adtsHeader.length;
        adtsHeader[3] |= (byte) ((size & 0x1800) >> 11);
        adtsHeader[4] = (byte) ((size & 0x1FF8) >> 3);
        adtsHeader[5] = (byte) ((size & 0x7) << 5);
        return adtsHeader;
    }

}
