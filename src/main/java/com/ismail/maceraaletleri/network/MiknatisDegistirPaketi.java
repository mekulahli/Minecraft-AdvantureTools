package com.ismail.maceraaletleri.network;

import com.ismail.maceraaletleri.MaceraAletleri;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// İstemciden sunucuya: "mıknatıs yüzüğümü aç/kapat". İçinde veri yok, paketin gelmesi yeterli.
public record MiknatisDegistirPaketi() implements CustomPacketPayload {
    public static final MiknatisDegistirPaketi INSTANCE = new MiknatisDegistirPaketi();

    public static final Type<MiknatisDegistirPaketi> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "miknatis_degistir"));

    public static final StreamCodec<ByteBuf, MiknatisDegistirPaketi> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
