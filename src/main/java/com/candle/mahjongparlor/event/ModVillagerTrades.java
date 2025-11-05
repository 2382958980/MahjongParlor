package com.candle.mahjongparlor.event;

import com.candle.mahjongparlor.MahjongParlor;
import com.candle.mahjongparlor.item.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MahjongParlor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModVillagerTrades {


    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) return;

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        int targetLevel = 5;
        List<VillagerTrades.ItemListing> levelList = trades.get(targetLevel);

        VillagerTrades.ItemListing occasionalListing = (entity, random) -> {
            if (random.nextFloat() < 0.99F) {
                ItemStack sell = new ItemStack(Items.EMERALD_BLOCK, 64);
                ItemStack buy = new ItemStack(ModItems.AFRICAHEART.get(), 1);

                int maxUses = 12;      // 此交易最大可使用次数（你可以调整）
                int xp = 5;            // 村民获得经验
                float priceMult = 0F; // 价格浮动系数

                return new MerchantOffer(buy, sell, maxUses, xp, priceMult);
            }
            // 返回 null 表示这次不生成该交易
            return null;
        };

        // 将条目加入该等级交易列表（注意：如果你希望放在其他等级，改 targetLevel）
        levelList.add(occasionalListing);
    }
}
