package de.skyslycer.hmcwraps.listener;

import de.skyslycer.hmcwraps.HMCWraps;
import de.skyslycer.hmcwraps.permission.PermissionHelper;
import de.skyslycer.hmcwraps.serialization.IWrappableItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final HMCWraps plugin;

    public InventoryClickListener(HMCWraps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getWhoClicked().getInventory() || event.getCurrentItem() == null
                || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        var player = (Player) event.getWhoClicked();
        var target = event.getCurrentItem().clone();

        Bukkit.getScheduler().runTaskLater(plugin, () -> PermissionHelper.loopThroughInventory(plugin, player), 1L);

        if (event.getCursor() == null) {
            return;
        }

        var physical = event.getCursor().clone();
        var cursor = physical.clone();
        if (cursor.getAmount() != 1) {
            cursor.setAmount(cursor.getAmount() - 1);
        } else {
            cursor = null;
        }

        if (plugin.getWrapper().isUnwrapper(physical) && plugin.getWrapper().getWrap(target) != null) {
            event.setCurrentItem(plugin.getWrapper().removeWrap(target, player, true));
            event.setCursor(cursor);
            event.setCancelled(true);
            return;
        }

        var wrapId = plugin.getWrapper().getWrapper(physical);
        if (wrapId == null) {
            return;
        }

        var wrap = plugin.getWraps().get(wrapId);
        if (wrap == null) {
            return;
        }
        var finalCursor = cursor;
        if (wrap.getPhysical() != null && (wrap.hasPermission(player) || !plugin.getConfiguration().getPermissionSettings().isPermissionPhysical())) {
            for (IWrappableItem wrappableItem : plugin.getCollection().getItems(target.getType())) {
                if (wrappableItem.getWraps().containsValue(wrap)) {
                    event.setCurrentItem(plugin.getWrapper().setWrap(wrap.getModelId(), wrap.getUuid(), target, true,
                            player, true));
                    event.setCursor(finalCursor);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
