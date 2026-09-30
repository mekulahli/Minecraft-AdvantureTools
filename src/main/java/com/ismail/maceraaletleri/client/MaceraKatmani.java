package com.ismail.maceraaletleri.client;

import com.ismail.maceraaletleri.MaceraAletleri;
import com.ismail.maceraaletleri.ModItems;
import com.ismail.maceraaletleri.event.HareketOlaylari;
import com.ismail.maceraaletleri.item.SirtCantasiItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// Oyuncu modeline eklenen katman: sırta takılı çantayı sırtta, açık planörü başın üstünde çizer.
// Minecraft 26.x'te çizim iki aşamalı: önce "veriCikar" oyuncudan gereken bilgiyi render state'e kopyalar,
// sonra "submit" sadece o bilgiyle çizer.
public class MaceraKatmani extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final ContextKey<ItemStackRenderState> CANTA =
            new ContextKey<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "canta"));
    public static final ContextKey<ItemStackRenderState> PLANOR =
            new ContextKey<>(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "planor"));

    private static final float PIKSEL = 1.0F / 16.0F;

    public MaceraKatmani(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    // Her karede, çizimden önce çalışır (MaceraAletleriClient'ta kaydedilir).
    public static void veriCikar(AbstractClientPlayer oyuncu, AvatarRenderState state) {
        ItemStack canta = oyuncu.getItemBySlot(EquipmentSlot.CHEST);
        state.setRenderData(CANTA, canta.getItem() instanceof SirtCantasiItem ? modelHazirla(oyuncu, canta) : null);
        state.setRenderData(PLANOR, HareketOlaylari.planorAcik(oyuncu)
                ? modelHazirla(oyuncu, new ItemStack(ModItems.PLANOR.get())) : null);
    }

    private static ItemStackRenderState modelHazirla(AbstractClientPlayer oyuncu, ItemStack stack) {
        ItemStackRenderState model = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(model, stack, ItemDisplayContext.NONE, oyuncu);
        return model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int isik, AvatarRenderState state,
                       float yRot, float xRot) {
        // Bu katmanın koordinatlarında y aşağıyı, -z oyuncunun önünü gösterir.
        ItemStackRenderState canta = state.getRenderData(CANTA);
        if (canta != null) {
            poseStack.pushPose();
            this.getParentModel().body.translateAndRotate(poseStack); // eğilince çanta da eğilsin
            poseStack.translate(0.0F, 5 * PIKSEL, 5 * PIKSEL);
            poseStack.scale(1.0F, -1.0F, -1.0F); // modeli ters çevir: cebi dışarı baksın
            canta.submit(poseStack, collector, isik, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        ItemStackRenderState planor = state.getRenderData(PLANOR);
        if (planor != null) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -1.15F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F)); // yatay, burnu hafif yukarıda
            poseStack.scale(2.5F, 2.5F, 2.5F);
            planor.submit(poseStack, collector, isik, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
