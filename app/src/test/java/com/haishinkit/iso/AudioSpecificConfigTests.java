package com.haishinkit.iso;

import com.haishinkit.util.Log;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public final class AudioSpecificConfigTests {
    @Test
    public void main() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte)0x11, (byte)0x88});
        AudioSpecificConfig config = new AudioSpecificConfig(buffer);
        assertEquals(AudioSpecificConfig.AudioObjectType.AAC_LC, config.getType());
        assertEquals(AudioSpecificConfig.ChannelConfiguration.FRONT_OF_CENTER, config.getChannel());
        assertEquals(AudioSpecificConfig.SamplingFrequency.HZ48000, config.getFrequency());
    }
}
