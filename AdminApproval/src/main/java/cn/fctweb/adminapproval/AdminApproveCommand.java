package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AdminApproveCommand implements CommandExecutor {
    private static final String PERMISSION_APPROVE = "adminapproval.approve";

    private final RequestStore requestStore;

    public AdminApproveCommand(RequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§c你没有审批权限。§r");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§e用法: /adminapprove <申请编号>§r");
            return true;
        }

        int requestId;
        try {
            requestId = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("§c申请编号必须是数字。§r");
            return true;
        }

        ApprovalRequest request = this.requestStore.remove(requestId);
        if (request == null) {
            sender.sendMessage("§c未找到申请 #" + requestId + "。§r");
            return true;
        }

        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), request.command());
        if (success) {
            sender.sendMessage("§a申请 #" + request.id() + " 已审批并执行：/" + request.command() + "§r");
        } else {
            sender.sendMessage("§c申请 #" + request.id() + " 审批通过，但执行失败。§r");
        }

        Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            if (success) {
                requester.sendMessage("§a你的申请 #" + request.id() + " 已通过并执行。§r");
            } else {
                requester.sendMessage("§c你的申请 #" + request.id() + " 已通过，但执行失败。§r");
            }
        }

        return true;
    }
}
