package me.pineacle.signatures.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.pineacle.signatures.Signature;
import me.pineacle.signatures.SignaturesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SignedItemListener implements Listener {

    private final SignaturesPlugin plugin;

    public SignedItemListener(SignaturesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void dragOnItem(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return; // why
        Player p = (Player) e.getWhoClicked();

        if (!e.getInventory().getType().equals(InventoryType.CRAFTING))
            return; // ignore everything but the players inventory.

        // dragging / whats on the cursor prior to clicking current
        ItemStack cursor = e.getCursor();
        if (cursor == null) return;
        // being dropped on
        ItemStack current = e.getCurrentItem();
        if (current == null) return;

        if (current.getType().equals(Material.AIR) || cursor.getType().equals(Material.AIR))
            return; // drag/drop only on an item, not air

        NBTItem cursorNBT = new NBTItem(cursor);
        NBTItem currentNBT = new NBTItem(current);

        // name tag
        if (cursorNBT.hasKey("signature-item")) {
            if (currentNBT.hasKey("signature-item")) {
                return;
            }
            String signature = cursorNBT.getString("signature-item");

            // new Signature(plugin, p, current).sign(signature);

            Signature toSign = new Signature(plugin, p, current);
            toSign.sign(signature);

            if (toSign.isFit())
                cursor.setAmount(cursor.getAmount() - 1);

        }


    }

}
