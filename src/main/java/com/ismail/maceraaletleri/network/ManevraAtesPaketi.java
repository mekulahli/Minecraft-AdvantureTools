package com.ismail.maceraaletleri.network;

import com.ismail.maceraaletleri.MaceraAletleri;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// İstemciden sunucuya: "3D manevra takımının sol/sağ kancasını at (ya da geri çek)".
public record ManevraAtesPaketi(boolean sol) implements CustomPacketPayload {
    public static final Type<ManevraAtesPaketi> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "manevra_ates"));

    public static final StreamCodec<ByteBuf, ManevraAtesPaketi> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ManevraAtesPaketi::sol, ManevraAtesPaketi::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
