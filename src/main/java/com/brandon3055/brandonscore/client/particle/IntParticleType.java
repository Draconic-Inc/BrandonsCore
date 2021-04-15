package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by brandon3055 on 27/2/20.
 */
public class IntParticleType extends ParticleType<IntParticleType.IntParticleData> {

    private static IParticleData.IDeserializer<IntParticleData> DESERIALIZER = new IParticleData.IDeserializer<IntParticleData>() {
        @Override
        public IntParticleData fromCommand(ParticleType<IntParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            List<Integer> list = new ArrayList<>();
            while (reader.peek() == ' ') {
                reader.expect(' ');
                list.add((int) reader.readInt());
            }

            return new IntParticleData(particleTypeIn, DataUtils.toPrimitive(list.toArray(new Integer[0])));
        }

        @Override
        public IntParticleData fromNetwork(ParticleType<IntParticleData> particleTypeIn, PacketBuffer buffer) {
            return new IntParticleData(particleTypeIn, buffer.readByte());
        }
    };

    public IntParticleType(boolean alwaysShow) {
        super(alwaysShow, DESERIALIZER);
    }

    @Override
    public Codec<IntParticleData> codec() {
        return null;
    }

    public static class IntParticleData implements IParticleData {
        private ParticleType<?> type;
        private int[] data;

        public IntParticleData(ParticleType<?> type, int... data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public ParticleType<?> getType() {
            return type;
        }

        @Override
        public void writeToNetwork(PacketBuffer buffer) {
            buffer.writeVarIntArray(data);
        }

        public int[] get() {
            return data;
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %sb", Registry.PARTICLE_TYPE.getKey(this.getType()), DataUtils.stringArrayConcat(DataUtils.arrayToString(data), " "));
        }
    }
}
