package cn.fctweb.adminapproval;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class AdminRequestsCommand implements CommandExecutor {
    private static final String PERMISSION_APPROVE = "adminapproval.approve";

    private final RequestStore requestStore;

    public AdminRequestsCommand(RequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_APPROVE)) {
            sender.sendMessage("§c你没有查看申请列表的权限。§r");
            return true;
        }

        List<ApprovalRequest> requests = this.requestStore.listPending();
        if (requests.isEmpty()) {
            sender.sendMessage("§a当前没有待审批申请。§r");
            return true;
        }

        sender.sendMessage("§6待审批申请列表：§r");
        Instant now = Instant.now();
        for (ApprovalRequest request : requests) {
            long minutes = Duration.between(request.createdAt(), now).toMinutes();
            sender.sendMessage("§e#" + request.id() + "§r " + request.requesterName()
                    + " -> /" + request.command() + " §7(" + minutes + " 分钟前)§r");
        }
        return true;
    }
}
