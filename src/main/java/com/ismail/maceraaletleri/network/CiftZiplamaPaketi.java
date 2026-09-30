package com.ismail.maceraaletleri.network;

import com.ismail.maceraaletleri.MaceraAletleri;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// İstemciden sunucuya: "havada ikinci kez zıpladım". Hareketi istemci yapar; sunucu düşme hasarını
// sıfırlar, botu aşındırır ve sesi herkese duyurur.
public record CiftZiplamaPaketi() implements CustomPacketPayload {
    public static final CiftZiplamaPaketi INSTANCE = new CiftZiplamaPaketi();

    public static final Type<CiftZiplamaPaketi> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "cift_ziplama"));

    public static final StreamCodec<ByteBuf, CiftZiplamaPaketi> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
