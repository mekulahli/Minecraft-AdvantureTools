package com.ismail.maceraaletleri.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.ismail.maceraaletleri.MaceraAletleri;
import com.ismail.maceraaletleri.ModEntities;
import com.ismail.maceraaletleri.ModMenus;
import com.ismail.maceraaletleri.item.ManevraTakimiItem;
import com.ismail.maceraaletleri.item.ZiplamaBotuItem;
import com.ismail.maceraaletleri.network.CantaAcPaketi;
import com.ismail.maceraaletleri.network.CiftZiplamaPaketi;
import com.ismail.maceraaletleri.network.ManevraAtesPaketi;
import com.ismail.maceraaletleri.network.MiknatisDegistirPaketi;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

// Sadece istemcide (oyuncunun bilgisayarında) çalışan kodlar. Sunucuda bu sınıf hiç yüklenmez.
@Mod(value = MaceraAletleri.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MaceraAletleri.MODID, value = Dist.CLIENT)
public final class MaceraAletleriClient {
    private static final KeyMapping.Category KATEGORI =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "aletler"));

    public static final KeyMapping MIKNATIS_TUSU =
            new KeyMapping("key.maceraaletleri.miknatis", GLFW.GLFW_KEY_M, KATEGORI);
    public static final KeyMapping CANTA_TUSU =
            new KeyMapping("key.maceraaletleri.canta", GLFW.GLFW_KEY_B, KATEGORI);

    private static final double CIFT_ZIPLAMA_GUCU = 0.55;

    // Çift zıplama durumu (sadece bu bilgisayardaki oyuncu için).
    private static int kalanZiplama;
    private static boolean oncekiZiplamaTusu;

    public MaceraAletleriClient(ModContainer container) {
        // Mods → Macera Aletleri → Config ekranı.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // --- Kayıtlar ---

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KANCA.get(), KancaRenderer::new);
        event.registerEntityRenderer(ModEntities.ZIPLINE.get(), ZiplineRenderer::new);
        // Fişek karanlıkta da parlak görünsün (fullBright).
        event.registerEntityRenderer(ModEntities.ISARET_FISEGI.get(), context -> new ThrownItemRenderer<>(context, 1.0F, true));
    }

    // Sırttaki çanta ve açık planör için oyuncu modeline katman ekle.
    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType tip : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(tip);
            if (renderer != null) {
                renderer.addLayer(new MaceraKatmani(renderer));
            }
        }
    }

    // AvatarRenderer mankenleri de çizer; bu yüzden tipsiz kaydedip oyuncu olup olmadığını kendimiz kontrol ediyoruz.
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void onRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        var rendererSinifi = (Class<? extends EntityRenderer<? extends Entity, ? extends EntityRenderState>>) (Class<?>) AvatarRenderer.class;
        event.<Entity, EntityRenderState>registerEntityModifier(rendererSinifi, (entity, state) -> {
            if (entity instanceof AbstractClientPlayer oyuncu && state instanceof AvatarRenderState oyuncuState) {
                MaceraKatmani.veriCikar(oyuncu, oyuncuState);
            }
        });
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SIRT_CANTASI.get(), SirtCantasiScreen::new);
    }

    @SubscribeEvent
    static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(KATEGORI);
        event.register(MIKNATIS_TUSU);
        event.register(CANTA_TUSU);
    }

    // --- Tuşlar ve çift zıplama ---

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        while (MIKNATIS_TUSU.consumeClick()) {
            ClientPacketDistributor.sendToServer(MiknatisDegistirPaketi.INSTANCE);
        }
        while (CANTA_TUSU.consumeClick()) {
            ClientPacketDistributor.sendToServer(CantaAcPaketi.INSTANCE);
        }
        ciftZiplama();
    }

    // Havadayken zıplama tuşuna yeniden basılırsa (basılı tutmak sayılmaz) bir kez daha zıpla.
    private static void ciftZiplama() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer oyuncu = mc.player;
        if (oyuncu == null) {
            return;
        }
        boolean tus = mc.options.keyJump.isDown();
        boolean botGiyili = oyuncu.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ZiplamaBotuItem;

        if (oyuncu.onGround() || oyuncu.onClimbable() || oyuncu.isInWater()) {
            kalanZiplama = 1;
        } else if (tus && !oncekiZiplamaTusu && kalanZiplama > 0 && botGiyili
                && !oyuncu.getAbilities().flying && !oyuncu.isFallFlying() && !oyuncu.isPassenger()
                && !oyuncu.isShiftKeyDown() && !(oyuncu.getMainHandItem().getItem() instanceof ManevraTakimiItem)) {
            kalanZiplama--;
            Vec3 hiz = oyuncu.getDeltaMovement();
            oyuncu.setDeltaMovement(hiz.x, CIFT_ZIPLAMA_GUCU, hiz.z);
            for (int i = 0; i < 12; i++) {
                double aci = i * Math.PI / 6;
                oyuncu.level().addParticle(ParticleTypes.CLOUD, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                        Math.cos(aci) * 0.15, -0.05, Math.sin(aci) * 0.15);
            }
            ClientPacketDistributor.sendToServer(CiftZiplamaPaketi.INSTANCE);
        }
        oncekiZiplamaTusu = tus;
    }

    // --- 3D manevra takımı: sol tık = sol kanca, sağ tık = sağ kanca ---
    // Normal saldırı/kullanma iptal edilir, yerine sunucuya paket gönderilir.
    @SubscribeEvent
    static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer oyuncu = Minecraft.getInstance().player;
        if (oyuncu == null || event.getHand() != InteractionHand.MAIN_HAND
                || !(oyuncu.getMainHandItem().getItem() instanceof ManevraTakimiItem)) {
            return;
        }
        if (event.isAttack() || event.isUseItem()) {
            ClientPacketDistributor.sendToServer(new ManevraAtesPaketi(event.isAttack()));
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    // --- Kullanım ipuçları: Shift basılıyken eşyanın nasıl kullanıldığını göster ---
    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!MaceraAletleri.MODID.equals(id.getNamespace())) {
            return;
        }
        String anahtar = "tooltip.maceraaletleri." + id.getPath() + ".kullanim.";
        Language dil = Language.getInstance();
        if (!dil.has(anahtar + 1)) {
            return;
        }
        List<Component> satirlar = event.getToolTip();
        if (Minecraft.getInstance().hasShiftDown()) {
            for (int i = 1; dil.has(anahtar + i); i++) {
                satirlar.add(Component.translatable(anahtar + i).withStyle(ChatFormatting.GRAY));
            }
        } else {
            satirlar.add(Component.translatable("tooltip.maceraaletleri.shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
