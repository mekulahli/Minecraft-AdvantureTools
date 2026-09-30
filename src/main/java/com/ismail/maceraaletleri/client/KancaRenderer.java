package com.ismail.maceraaletleri.client;

import com.ismail.maceraaletleri.entity.KancaEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

// Kancayı (eşyanın kendi modeliyle) ve oyuncunun elinden kancaya uzanan ipi çizer.
// Vanilla FishingHookRenderer'dan uyarlandı.
public class KancaRenderer extends EntityRenderer<KancaEntity, KancaRenderState> {
    private static final int IP_RENGI = 0xFF4A3520; // ARGB: koyu kahverengi

    private final ItemModelResolver itemModelResolver;

    public KancaRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public KancaRenderState createRenderState() {
        return new KancaRenderState();
    }

    @Override
    public void extractRenderState(KancaEntity kanca, KancaRenderState state, float partialTicks) {
        super.extractRenderState(kanca, state, partialTicks);
        this.itemModelResolver.updateForNonLiving(state.item, kanca.getItem(), ItemDisplayContext.GROUND, kanca);

        if (kanca.getOwner() instanceof Player sahip) {
            Vec3 baslangic = kanca.isManevra()
                    ? belPozisyonu(sahip, kanca.getTaraf() == KancaEntity.TARAF_SAG ? 1 : -1, partialTicks)
                    : elPozisyonu(sahip, partialTicks);
            state.ipVektoru = baslangic.subtract(kanca.getPosition(partialTicks));
        } else {
            state.ipVektoru = Vec3.ZERO;
        }
    }

    @Override
    public void submit(KancaRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // Kancanın kendisi: her zaman kameraya dönük düz bir eşya resmi (kartopu gibi).
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();

        // İp: kancadan oyuncunun eline düz bir çizgi.
        Vec3 ip = state.ipVektoru;
        if (ip.lengthSqr() > 0.0) {
            Vec3 yon = ip.normalize();
            float kalinlik = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(IP_RENGI)
                        .setNormal(pose, (float) yon.x, (float) yon.y, (float) yon.z).setLineWidth(kalinlik);
                buffer.addVertex(pose, (float) ip.x, (float) ip.y, (float) ip.z).setColor(IP_RENGI)
                        .setNormal(pose, (float) yon.x, (float) yon.y, (float) yon.z).setLineWidth(kalinlik);
            });
        }

        super.submit(state, poseStack, collector, camera);
    }

    // Kanca ekran dışındayken bile ip görünsün.
    @Override
    protected boolean affectedByCulling(KancaEntity kanca) {
        return false;
    }

    // 3D manevra takımının kancaları kalçanın iki yanından çıkar. yon: 1 = sağ, -1 = sol.
    private Vec3 belPozisyonu(Player sahip, int yon, float partialTicks) {
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && sahip == Minecraft.getInstance().player) {
            float fov = this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 ekrandaBel = this.entityRenderDispatcher.camera
                    .getNearPlane(fov)
                    .getPointOnPlane(yon * 0.8F, -0.9F)
                    .scale(960.0 / fov);
            return sahip.getEyePosition(partialTicks).add(ekrandaBel);
        }

        float govdeAcisi = Mth.lerp(partialTicks, sahip.yBodyRotO, sahip.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(govdeAcisi);
        double cos = Mth.cos(govdeAcisi);
        double sagaKayma = yon * 0.3;
        return sahip.getPosition(partialTicks).add(-cos * sagaKayma, 0.8, -sin * sagaKayma);
    }

    // Oyuncunun kancayı tuttuğu elin dünyadaki yaklaşık konumu.
    private Vec3 elPozisyonu(Player sahip, float partialTicks) {
        boolean sagEl = sahip.getMainArm() == HumanoidArm.RIGHT;
        int yon = sagEl ? 1 : -1;

        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && sahip == Minecraft.getInstance().player) {
            float fov = this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 ekrandakiEl = this.entityRenderDispatcher.camera
                    .getNearPlane(fov)
                    .getPointOnPlane(yon * 0.525F, -0.1F)
                    .scale(960.0 / fov);
            return sahip.getEyePosition(partialTicks).add(ekrandakiEl);
        }

        float govdeAcisi = Mth.lerp(partialTicks, sahip.yBodyRotO, sahip.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(govdeAcisi);
        double cos = Mth.cos(govdeAcisi);
        double sagaKayma = yon * 0.35;
        double ileriKayma = 0.8;
        return sahip.getEyePosition(partialTicks)
                .add(-cos * sagaKayma - sin * ileriKayma, -0.45, -sin * sagaKayma + cos * ileriKayma);
    }
}
