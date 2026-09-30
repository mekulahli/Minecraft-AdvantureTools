package com.ismail.maceraaletleri;

import com.ismail.maceraaletleri.block.IpMerdivenBlock;
import com.ismail.maceraaletleri.block.UykuTulumuBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

// Modun blokları.
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MaceraAletleri.MODID);

    // İp merdivenin parçaları. Eşyası ayrı (IpMerdivenItem); kırılınca bütün merdiven tek eşya olarak geri döner.
    public static final DeferredBlock<IpMerdivenBlock> IP_MERDIVEN = BLOCKS.registerBlock("ip_merdiven",
            IpMerdivenBlock::new, p -> p.forceSolidOff().strength(0.4F).sound(SoundType.LADDER)
                    .noOcclusion().pushReaction(PushReaction.DESTROY).noLootTable());

    public static final DeferredBlock<UykuTulumuBlock> UYKU_TULUMU = BLOCKS.registerBlock("uyku_tulumu",
            UykuTulumuBlock::new, p -> p.mapColor(MapColor.COLOR_GREEN).strength(0.2F).sound(SoundType.WOOL)
                    .noOcclusion().pushReaction(PushReaction.DESTROY));

    private ModBlocks() {
    }
}
