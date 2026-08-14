package cn.fctweb.adminapproval;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class DangerousCommandListener implements Listener {
    private final DangerousCommandPolicy policy;
    private final RequestStore requestStore;
    private final AccessControl accessControl;

    public DangerousCommandListener(DangerousCommandPolicy policy, RequestStore requestStore, AccessControl accessControl) {
        this.policy = policy;
        this.requestStore = requestStore;
        this.accessControl = accessControl;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!this.policy.requiresApproval(event.getMessage())) {
            return;
        }

        if (this.accessControl.isOwner(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);

        if (!this.accessControl.isAdmin(event.getPlayer())) {
            event.getPlayer().sendMessage("§c你不是管理员，无法提交危险命令审批。§r");
            return;
        }

        String requested = event.getMessage().trim();
        if (requested.startsWith("/")) {
            requested = requested.substring(1);
        }

        ApprovalRequest request = this.requestStore.create(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                requested
        );

        event.getPlayer().sendMessage("§a危险命令已转为审批申请，编号 #" + request.id() + "。§r");

        String line1 = "§6[AdminApproval] 新审批申请§r";
        String line2 = "§e编号: #" + request.id() + "§r";
        String line3 = "§e申请人: " + request.requesterName() + "§r";
        String line4 = "§e命令: " + request.command() + "§r";

        org.bukkit.Bukkit.getOnlinePlayers().stream()
                .filter(this.accessControl::isOwner)
                .forEach(owner -> {
                    owner.sendMessage(line1);
                    owner.sendMessage(line2);
                    owner.sendMessage(line3);
                    owner.sendMessage(line4);
                });
    }
}
