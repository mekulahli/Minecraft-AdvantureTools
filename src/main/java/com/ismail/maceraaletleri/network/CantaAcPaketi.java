package com.ismail.maceraaletleri.network;

import com.ismail.maceraaletleri.MaceraAletleri;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// İstemciden sunucuya: "sırtımdaki çantayı aç" (B tuşu).
public record CantaAcPaketi() implements CustomPacketPayload {
    public static final CantaAcPaketi INSTANCE = new CantaAcPaketi();

    public static final Type<CantaAcPaketi> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "canta_ac"));

    public static final StreamCodec<ByteBuf, CantaAcPaketi> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
