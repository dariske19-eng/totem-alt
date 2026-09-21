package com.totemalt;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class TotemAltClient implements ClientModInitializer {

    private static KeyBinding equipTotemKey;

    @Override
    public void onInitializeClient() {
        // Register keybind - default Left Alt
        equipTotemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.totem-alt.equip",          // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,         // default: Left Alt
                "category.totem-alt"            // category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (equipTotemKey.wasPressed()) {
                equipTotem(client);
            }
        });
    }

    private void equipTotem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (client.currentScreen != null) return; // jangan jalan kalau GUI terbuka

        PlayerInventory inv = client.player.getInventory();

        // Sudah ada totem di offhand? skip
        if (inv.offHand.get(0).isOf(Items.TOTEM_OF_UNDYING)) return;

        // Cari totem di inventory (0-35) termasuk hotbar
        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) return; // tidak ada totem

        // Slot mapping untuk clickSlot:
        // Hotbar 0-8  → slot 36-44
        // Main inv 9-35 → slot 9-35
        // Offhand → 45

        int fromSlot;
        if (totemSlot < 9) {
            fromSlot = 36 + totemSlot; // hotbar
        } else {
            fromSlot = totemSlot;      // main inventory
        }

        // Pindah item ke offhand menggunakan SWAP (sama seperti tekan F)
        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                fromSlot,
                40, // button 40 = swap with offhand
                SlotActionType.SWAP,
                client.player
        );
    }
}
