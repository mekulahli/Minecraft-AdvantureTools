package com.ismail.maceraaletleri.client;

import com.ismail.maceraaletleri.menu.SirtCantasiMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

// Sırt çantası penceresi: vanilla büyük sandık görünümünü (generic_54, en fazla 6 satır) kullanır. Vanilla ContainerScreen'den uyarlandı.
public class SirtCantasiScreen extends AbstractContainerScreen<SirtCantasiMenu> {
    private static final Identifier ARKA_PLAN = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public SirtCantasiScreen(SirtCantasiMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + menu.getSatir() * 18);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int cantaYuksekligi = this.menu.getSatir() * 18 + 17;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ARKA_PLAN, x, y, 0.0F, 0.0F, this.imageWidth, cantaYuksekligi, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ARKA_PLAN, x, y + cantaYuksekligi, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
    }
}
