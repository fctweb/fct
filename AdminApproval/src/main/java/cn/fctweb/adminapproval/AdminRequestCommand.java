package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class AdminRequestCommand implements CommandExecutor {
    private final DangerousCommandPolicy policy;
    private final RequestStore requestStore;
    private final AccessControl accessControl;

    public AdminRequestCommand(DangerousCommandPolicy policy, RequestStore requestStore, AccessControl accessControl) {
        this.policy = policy;
        this.requestStore = requestStore;
        this.accessControl = accessControl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以提交审批申请。§r");
            return true;
        }

        if (this.accessControl.isOwner(player)) {
            player.sendMessage("§a你是腐竹，危险命令无需审批可直接执行。§r");
            return true;
        }

        if (!this.accessControl.isAdmin(player)) {
            player.sendMessage("§c你不是管理员，无法提交危险命令审批。§r");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e用法: /adminrequest <危险命令...>§r");
            return true;
        }

        String requested = String.join(" ", args).trim();
        if (requested.startsWith("/")) {
            requested = requested.substring(1);
        }

        if (!this.policy.isDangerous(requested)) {
            player.sendMessage("§a该命令不属于危险命令，无需审批。§r");
            return true;
        }

        if (!this.policy.requiresApproval(requested)) {
            player.sendMessage("§a该危险命令已在免审批白名单中，可直接执行。§r");
            return true;
        }

        ApprovalRequest request = this.requestStore.create(player.getUniqueId(), player.getName(), requested);

        String submitMessage = "§a申请已提交，编号 #" + request.id() + "，等待腐竹审批。§r";
        player.sendMessage(submitMessage);

        Bukkit.getOnlinePlayers().stream()
                .filter(this.accessControl::isOwner)
                .forEach(online -> {
                    online.sendMessage("§6[AdminApproval] 新审批申请§r");
                    online.sendMessage("§e编号: #" + request.id() + "§r");
                    online.sendMessage("§e申请人: " + request.requesterName() + "§r");
                    online.sendMessage("§e命令: " + request.command() + "§r");
                });

        return true;
    }
}
