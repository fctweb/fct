package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class AdminRequestCommand implements CommandExecutor {
    public static final String PERMISSION_REQUEST = "adminapproval.request";
    public static final String PERMISSION_APPROVE = "adminapproval.approve";

    private final DangerousCommandPolicy policy;
    private final RequestStore requestStore;

    public AdminRequestCommand(DangerousCommandPolicy policy, RequestStore requestStore) {
        this.policy = policy;
        this.requestStore = requestStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以提交审批申请。§r");
            return true;
        }

        if (!player.hasPermission(PERMISSION_REQUEST)) {
            player.sendMessage("§c你没有提交危险命令申请的权限。§r");
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

        ApprovalRequest request = this.requestStore.create(player.getUniqueId(), player.getName(), requested);

        String submitMessage = "§a申请已提交，编号 #" + request.id() + "，等待腐竹审批。§r";
        player.sendMessage(submitMessage);

        String notifyMessage = "§6[AdminApproval] 新申请 #" + request.id() + " 来自 "
                + request.requesterName() + "：/" + request.command() + "§r";

        Bukkit.getOnlinePlayers().stream()
                .filter(online -> online.hasPermission(PERMISSION_APPROVE))
                .forEach(online -> online.sendMessage(notifyMessage));

        return true;
    }
}
