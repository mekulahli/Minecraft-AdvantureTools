package com.ismail.maceraaletleri;

import com.ismail.maceraaletleri.menu.SirtCantasiMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Envanter pencereleri (menüler).
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MaceraAletleri.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SirtCantasiMenu>> SIRT_CANTASI =
            MENUS.register("sirt_cantasi", () -> IMenuTypeExtension.create(SirtCantasiMenu::new));

    private ModMenus() {
    }
}
