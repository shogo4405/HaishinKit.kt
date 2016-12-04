package com.haishinkit.iso;

import android.media.MediaFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class AVCConfigurationRecord {
    public static final byte RESERVE_LENGTH_SIZE_MINUS_ONE = (byte) 0x3F;
    public static final byte RESERVE_NUM_OF_SEQUENCE_PARAMETER_SETS = (byte) 0xE0;
    public static final byte RESERVE_CHROME_FORMAT = (byte) 0xFC;
    public static final byte RESERVE_BIT_DEPTH_LUMA_MINUS8 = (byte) 0xF8;
    public static final byte RESERVE_BIT_DEPTH_CHROME_MINUS8 = (byte) 0xF8;

    private byte configurationVersion = 1;
    private byte AVCProfileIndication = 0;
    private byte profileCompatibility = 0;
    private byte AVCLevelIndication = 0;
    private byte lengthSizeMinusOneWithReserved = 0;
    private byte numOfSequenceParameterSetsWithReserved = 0;
    private List<byte[]> sequenceParameterSets = null;
    private List<byte[]> pictureParameterSets = null;

    /*
    private byte chromaFormatWithReserve = 0;
    private byte bitDepthLumaMinus8WithReserve = 0;
    private byte bitDepthChromaMinus8WithReserve = 0;
    private List<byte[]> sequenceParameterSetExt = null;
    */

    public AVCConfigurationRecord() {
    }

    public AVCConfigurationRecord(final MediaFormat mediaFormat) {
        // SPS => 0x00,0x00,0x00,0x01,0x67,0x42,0x00,0x29,0x8d,0x8d,0x40,0xa0,0xfd,0x00,0xf0,0x88,0x45,0x38
        ByteBuffer spsBuffer = mediaFormat.getByteBuffer("csd-0");
        // PPS => 0x00,0x00,0x00,0x01,0x68,0xca,0x43,0xc8
        ByteBuffer ppsBuffer = mediaFormat.getByteBuffer("csd-1");

        if (spsBuffer == null || ppsBuffer == null) {
            throw new IllegalStateException();
        }

        /**
         * SPS layout
         **
         profile_idc (8)
         constraint_set0_flag (1)
         constraint_set1_flag (1)
         constraint_set2_flag (1)
         constraint_set3_flag (1)
         constraint_set4_flag (1)
         reserved_zero_3bits (3)
         level_idc (8)
         ...
         */
        spsBuffer.position(4);
        byte[] spsBytes = new byte[spsBuffer.remaining()];
        spsBuffer.get(spsBytes);
        setConfigurationVersion((byte) 0x01);
        setAVCProfileIndication(spsBytes[1]);
        setProfileCompatibility(spsBytes[2]);
        setAVCLevelIndication(spsBytes[3]);
        setLengthSizeMinusOneWithReserved((byte) 0xFF);

        // SPS
        setNumOfSequenceParameterSetsWithReserved((byte) 0xE1);
        List<byte[]> spsList = new ArrayList<byte[]>(1);
        spsList.add(spsBytes);
        setSequenceParameterSets(spsList);

        // PPS
        ppsBuffer.position(4);
        byte[] ppsBytes = new byte[ppsBuffer.remaining()];
        ppsBuffer.get(ppsBytes);
        List<byte[]> ppsList = new ArrayList<byte[]>(1);
        ppsList.add(ppsBytes);
        setPictureParameterSets(ppsList);
    }

    public byte getConfigurationVersion() {
        return configurationVersion;
    }

    public AVCConfigurationRecord setConfigurationVersion(final byte configurationVersion) {
        this.configurationVersion = configurationVersion;
        return this;
    }

    public byte getAVCProfileIndication() {
        return AVCProfileIndication;
    }

    public AVCConfigurationRecord setAVCProfileIndication(final byte AVCProfileIndication) {
        this.AVCProfileIndication = AVCProfileIndication;
        return this;
    }

    public byte getProfileCompatibility() {
        return profileCompatibility;
    }

    public AVCConfigurationRecord setProfileCompatibility(final byte profileCompatibility) {
        this.profileCompatibility = profileCompatibility;
        return this;
    }

    public byte getAVCLevelIndication() {
        return AVCLevelIndication;
    }

    public AVCConfigurationRecord setAVCLevelIndication(final byte AVCLevelIndication) {
        this.AVCLevelIndication = AVCLevelIndication;
        return this;
    }

    public byte getLengthSizeMinusOneWithReserved() {
        return lengthSizeMinusOneWithReserved;
    }

    public AVCConfigurationRecord setLengthSizeMinusOneWithReserved(final byte lengthSizeMinusOneWithReserved) {
        this.lengthSizeMinusOneWithReserved = lengthSizeMinusOneWithReserved;
        return this;
    }

    public byte getNumOfSequenceParameterSetsWithReserved() {
        return numOfSequenceParameterSetsWithReserved;
    }

    public AVCConfigurationRecord setNumOfSequenceParameterSetsWithReserved(final byte numOfSequenceParameterSetsWithReserved) {
        this.numOfSequenceParameterSetsWithReserved = numOfSequenceParameterSetsWithReserved;
        return this;
    }

    public List<byte[]> getSequenceParameterSets() {
        return sequenceParameterSets;
    }

    public AVCConfigurationRecord setSequenceParameterSets(final List<byte[]> sequenceParameterSets) {
        this.sequenceParameterSets = sequenceParameterSets;
        return this;
    }

    public List<byte[]> getPictureParameterSets() {
        return pictureParameterSets;
    }

    public AVCConfigurationRecord setPictureParameterSets(final List<byte[]> pictureParameterSets) {
        this.pictureParameterSets = pictureParameterSets;
        return this;
    }

    public byte getNALULength() {
        return (byte)((getLengthSizeMinusOneWithReserved() >> 6) + 1);
    }

    public ByteBuffer toByteBuffer() {
        int capacity = 5;
        final List<byte[]> sequenceParameterSets = getSequenceParameterSets();
        final List<byte[]> pictureParameterSets = getPictureParameterSets();

        for (byte[] sps : sequenceParameterSets) {
            capacity += 3 + sps.length;
        }
        for (byte[] pps : pictureParameterSets) {
            capacity += 3 + pps.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(getConfigurationVersion());
        buffer.put(getAVCProfileIndication());
        buffer.put(getProfileCompatibility());
        buffer.put(getAVCLevelIndication());
        buffer.put(getLengthSizeMinusOneWithReserved());

        // SPS
        buffer.put(getNumOfSequenceParameterSetsWithReserved());
        for (byte[] sps : sequenceParameterSets) {
            buffer.putShort((short) sps.length);
            buffer.put(sps);
        }

        // PPS
        buffer.put((byte) pictureParameterSets.size());
        for (byte[] pps : pictureParameterSets) {
            buffer.putShort((short) pps.length);
            buffer.put(pps);
        }

        buffer.flip();

        return buffer;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
