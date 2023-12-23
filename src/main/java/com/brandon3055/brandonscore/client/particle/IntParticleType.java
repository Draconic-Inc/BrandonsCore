package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by brandon3055 on 27/2/20.
 */
public class IntParticleType extends ParticleType<IntParticleType.IntParticleData> {

    private static ParticleOptions.Deserializer<IntParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
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
        public IntParticleData fromNetwork(ParticleType<IntParticleData> particleTypeIn, FriendlyByteBuf buffer) {
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

    public static class IntParticleData implements ParticleOptions {
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
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeVarIntArray(data);
        }

        public int[] get() {
            return data;
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %sb", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), DataUtils.stringArrayConcat(DataUtils.arrayToString(data), " "));
        }
    }
}
