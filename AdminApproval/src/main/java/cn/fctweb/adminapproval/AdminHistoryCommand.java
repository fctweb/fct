package cn.fctweb.adminapproval;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class AdminHistoryCommand implements CommandExecutor {
    private static final String PERMISSION_APPROVE = "adminapproval.approve";

    private final RequestStore requestStore;

    public AdminHistoryCommand(RequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§c你没有查看审批历史的权限。§r");
            return true;
        }

        int limit = 10;
        if (args.length > 0) {
            try {
                limit = Math.max(1, Math.min(50, Integer.parseInt(args[0])));
            } catch (NumberFormatException ex) {
                sender.sendMessage("§c数量必须是数字。§r");
                return true;
            }
        }

        List<ApprovalHistoryEntry> history = this.requestStore.listHistory();
        if (history.isEmpty()) {
            sender.sendMessage("§a当前没有审批历史。§r");
            return true;
        }

        sender.sendMessage("§6最近审批历史：§r");
        Instant now = Instant.now();
        for (int i = 0; i < Math.min(limit, history.size()); i++) {
            ApprovalHistoryEntry entry = history.get(i);
            long minutes = Duration.between(entry.processedAt(), now).toMinutes();
            String result = entry.action().equals("REJECTED")
                    ? "§c已拒绝"
                    : (entry.executionSuccess() ? "§a已通过" : "§e通过但执行失败");
            sender.sendMessage("§e#" + entry.requestId() + "§r " + entry.requesterName()
                    + " -> /" + entry.command()
                    + " | " + result + "§r"
                    + " | 审批人: " + entry.reviewerName()
                    + " §7(" + minutes + " 分钟前)§r");
        }
        return true;
    }
}
