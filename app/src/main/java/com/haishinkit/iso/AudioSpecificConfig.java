package com.haishinkit.iso;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.util.ByteBufferUtils;
import com.haishinkit.util.Log;

import org.apache.commons.lang3.builder.ToStringBuilder;
import java.nio.ByteBuffer;

/**
 * The Audio Specific Config is the global header for MPEG-4 Audio
 *
 * @see http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio#Audio_Specific_Config
 * @see http://wiki.multimedia.cx/?title=Understanding_AAC
 */
public final class AudioSpecificConfig {
    public static final int ADTS_HEADER_SIZE = 7;

    public enum AudioObjectType implements IRawValue<Byte> {
        UNKNOWN((byte) 0x00),
        AAC_MAIN((byte) 0x01),
        AAC_LC((byte) 0x02),
        AAC_SSL((byte) 0x03),
        AAC_LTP((byte) 0x04),
        AAC_SBR((byte) 0x05),
        AAC_SCALABLE((byte) 0x06),
        TWINQVQ((byte) 0x07),
        CELP((byte) 0x08),
        HXVC((byte) 0x09);

        public static AudioObjectType rawValue(final byte rawValue) {
            switch (rawValue) {
                case 0x01:
                    return AAC_MAIN;
                case 0x02:
                    return AAC_LC;
                case 0x03:
                    return AAC_SSL;
                case 0x04:
                    return AAC_LTP;
                case 0x05:
                    return AAC_SBR;
                case 0x06:
                    return AAC_SCALABLE;
                case 0x07:
                    return TWINQVQ;
                case 0x08:
                    return CELP;
                case 0x09:
                    return HXVC;
            }
            return UNKNOWN;
        }

        private final byte rawValue;

        AudioObjectType(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    public enum SamplingFrequency implements IRawValue<Byte> {
        HZ96000((byte) 0x00),
        HZ88200((byte) 0x01),
        HZ64000((byte) 0x02),
        HZ48000((byte) 0x03),
        HZ44100((byte) 0x04),
        HZ32000((byte) 0x05),
        HZ24000((byte) 0x06),
        HZ22050((byte) 0x07),
        HZ16000((byte) 0x08),
        HZ12000((byte) 0x09),
        HZ11025((byte) 0x0A),
        HZ8000((byte) 0x0B),
        HZ7350((byte) 0x0C);

        public static SamplingFrequency rawValue(final byte rawValue) {
            switch (rawValue) {
                case 0x00:
                    return HZ96000;
                case 0x01:
                    return HZ88200;
                case 0x02:
                    return HZ64000;
                case 0x03:
                    return HZ48000;
                case 0x04:
                    return HZ44100;
                case 0x05:
                    return HZ32000;
                case 0x06:
                    return HZ24000;
                case 0x07:
                    return HZ22050;
                case 0x08:
                    return HZ16000;
                case 0x09:
                    return HZ12000;
                case 0x0A:
                    return HZ11025;
                case 0x0B:
                    return HZ8000;
                case 0x0C:
                    return HZ7350;
            }
            return HZ96000;
        }

        private final byte rawValue;

        SamplingFrequency(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    public enum ChannelConfiguration implements IRawValue<Byte> {
        DEFINE_IN_AOT_SPECIFIC_CONFIG((byte) 0x00),
        FRONT_OF_CENTER((byte) 0x01),
        FRONT_LEFT_AND_FRONT_RIGHT((byte) 0x02),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT((byte) 0x03),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_CENTER((byte) 0x04),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT((byte) 0x05),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT_LFE((byte) 0x06),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_SIDE_LEFT_AND_RIGHT_AND_BACK_RIGHT_LFE((byte) 0x07),
        UNKNOWN((byte) 0xFF);

        public static ChannelConfiguration rawValue(final byte rawValue) {
            switch (rawValue) {
                case 0x00:
                    return DEFINE_IN_AOT_SPECIFIC_CONFIG;
                case 0x01:
                    return FRONT_OF_CENTER;
                case 0x02:
                    return FRONT_LEFT_AND_FRONT_RIGHT;
                case 0x03:
                    return FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT;
                case 0x04:
                    return FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_CENTER;
                case 0x05:
                    return FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT;
                case 0x06:
                    return FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT_LFE;
                case 0x07:
                    return FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_SIDE_LEFT_AND_RIGHT_AND_BACK_RIGHT_LFE;
                default:
                    return UNKNOWN;
            }
        }

        private final byte rawValue;

        ChannelConfiguration(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    private final AudioObjectType type;
    private final SamplingFrequency frequency;
    private final ChannelConfiguration channel;

    public AudioSpecificConfig(final AudioObjectType type, final SamplingFrequency frequency, final ChannelConfiguration channel) {
        this.type = type;
        this.frequency = frequency;
        this.channel = channel;
    }

    public AudioSpecificConfig(final ByteBuffer buffer) {
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        type = AudioObjectType.rawValue((byte) (bytes[0] >> 3));
        frequency = SamplingFrequency.rawValue((byte) (((bytes[0] & 0b00000111) << 1) | (bytes[1] & 0xFF) >> 7));
        channel = ChannelConfiguration.rawValue((byte) ((bytes[1] & 0b01111000) >> 3));
        buffer.flip();
    }

    public final AudioObjectType getType() {
        return type;
    }

    public final SamplingFrequency getFrequency() {
        return frequency;
    }

    public final ChannelConfiguration getChannel() {
        return channel;
    }

    public final byte[] toADTS(final int length) {
        final int fullSize = ADTS_HEADER_SIZE + length;
        byte[] adts = new byte[ADTS_HEADER_SIZE];
        adts[0] = (byte) 0xFF;
        adts[1] = (byte) 0xF9;
        adts[2] = (byte) (((type.rawValue() - 1) << 6) | (frequency.rawValue() << 2) | (channel.rawValue() >> 2));
        adts[3] = (byte) ((channel.rawValue() & 3) | fullSize >> 11);
        adts[4] = (byte) ((fullSize & 0x7FF) >> 3);
        adts[5] = (byte) (((fullSize & 7) << 5) | 0x1F);
        adts[6] = (byte) 0xFC;
        return adts;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
