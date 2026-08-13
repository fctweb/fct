package cn.fctweb.adminapproval;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class DangerousCommandListener implements Listener {
    private static final String PERMISSION_REQUEST = "adminapproval.request";
    private static final String PERMISSION_BYPASS = "adminapproval.bypass";

    private final DangerousCommandPolicy policy;

    public DangerousCommandListener(DangerousCommandPolicy policy) {
        this.policy = policy;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!this.policy.isDangerous(event.getMessage())) {
            return;
        }

        if (event.getPlayer().hasPermission(PERMISSION_BYPASS)) {
            return;
        }

        event.setCancelled(true);

        if (event.getPlayer().hasPermission(PERMISSION_REQUEST)) {
            event.getPlayer().sendMessage("§c危险命令已拦截，请使用 /adminrequest <命令...> 提交审批。§r");
            return;
        }

        event.getPlayer().sendMessage("§c你没有执行该危险命令的权限。§r");
    }
}
