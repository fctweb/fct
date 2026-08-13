package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AdminRejectCommand implements CommandExecutor {
    private static final String PERMISSION_APPROVE = "adminapproval.approve";

    private final RequestStore requestStore;

    public AdminRejectCommand(RequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§c你没有审批权限。§r");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§e用法: /adminreject <申请编号>§r");
            return true;
        }

        int requestId;
        try {
            requestId = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("§c申请编号必须是数字。§r");
            return true;
        }

        ApprovalRequest request = this.requestStore.removePending(requestId);
        if (request == null) {
            sender.sendMessage("§c未找到申请 #" + requestId + "。§r");
            return true;
        }

        this.requestStore.recordRejected(request, sender.getName());
        sender.sendMessage("§e已拒绝申请 #" + request.id() + "。§r");

        Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            requester.sendMessage("§c你的申请 #" + request.id() + " 已被拒绝。§r");
        }

        return true;
    }
}
