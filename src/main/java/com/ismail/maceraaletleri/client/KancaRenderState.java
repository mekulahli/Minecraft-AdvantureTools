package com.ismail.maceraaletleri.client;

import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.world.phys.Vec3;

// Çizim anında gereken veriler: kancanın kendisi (item) + ipin kancadan oyuncunun eline uzanan vektörü.
public class KancaRenderState extends ThrownItemRenderState {
    public Vec3 ipVektoru = Vec3.ZERO;
}
