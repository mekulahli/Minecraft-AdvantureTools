package com.ismail.maceraaletleri.client;

import com.ismail.maceraaletleri.ModItems;
import com.ismail.maceraaletleri.entity.ZiplineEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// Zipline ucunu (küçük bir makara) ve ana uçtan öbür uca gerilen hattı çizer.
public class ZiplineRenderer extends EntityRenderer<ZiplineEntity, ZiplineRenderer.Durum> {
    private static final int HAT_RENGI = 0xFF3B2A1A;
    private static final int HAT_PARLAK = 0xFF7A5A36;

    private final ItemModelResolver itemModelResolver;

    public static class Durum extends ThrownItemRenderState {
        public Vec3 hatVektoru = Vec3.ZERO;
    }

    public ZiplineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public Durum createRenderState() {
        return new Durum();
    }

    @Override
    public void extractRenderState(ZiplineEntity uc, Durum state, float partialTicks) {
        super.extractRenderState(uc, state, partialTicks);
        this.itemModelResolver.updateForNonLiving(state.item, new ItemStack(ModItems.ZIPLINE_MAKARASI.get()),
                ItemDisplayContext.GROUND, uc);
        state.hatVektoru = uc.isAnaUc()
                ? ZiplineEntity.baglantiNoktasi(uc.getBitis()).subtract(uc.position())
                : Vec3.ZERO;
    }

    @Override
    public void submit(Durum state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.15F, 0.0F);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(camera.orientation);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();

        Vec3 hat = state.hatVektoru;
        if (hat.lengthSqr() > 0.0) {
            Vec3 yon = hat.normalize();
            float kalinlik = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            // Birbirine çok yakın iki çizgi: tek çizgiden daha kalın ve ip gibi görünür.
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                for (int i = 0; i < 2; i++) {
                    float kayma = 0.03F * i;
                    int renk = i == 0 ? HAT_RENGI : HAT_PARLAK;
                    buffer.addVertex(pose, 0.0F, kayma, 0.0F).setColor(renk)
                            .setNormal(pose, (float) yon.x, (float) yon.y, (float) yon.z).setLineWidth(kalinlik);
                    buffer.addVertex(pose, (float) hat.x, (float) hat.y + kayma, (float) hat.z).setColor(renk)
                            .setNormal(pose, (float) yon.x, (float) yon.y, (float) yon.z).setLineWidth(kalinlik);
                }
            });
        }

        super.submit(state, poseStack, collector, camera);
    }

    // Uç ekran dışındayken bile hat görünsün.
    @Override
    protected boolean affectedByCulling(ZiplineEntity uc) {
        return false;
    }
}
