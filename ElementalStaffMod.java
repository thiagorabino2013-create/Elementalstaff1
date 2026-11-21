package dev.elementalstaff;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ElementalStaffMod implements ModInitializer {
    public static final String MOD_ID = "elementalstaff";

    public static final Item ELEMENTAL_STAFF = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "elemental_staff"),
            new ElementalStaffItem(
                    new Item.Settings().registryKey(
                            RegistryKey.of(Registries.ITEM.getKey(),
                                    Identifier.of(MOD_ID, "elemental_staff")))
                            .maxCount(1)
            )
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> entries.add(ELEMENTAL_STAFF));
    }
}
