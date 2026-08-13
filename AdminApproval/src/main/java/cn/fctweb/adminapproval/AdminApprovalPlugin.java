package cn.fctweb.adminapproval;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdminApprovalPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        DangerousCommandPolicy policy = new DangerousCommandPolicy();
        RequestStore requestStore = new RequestStore();

        this.getServer().getPluginManager().registerEvents(new DangerousCommandListener(policy), this);

        registerRequiredCommand("adminrequest", new AdminRequestCommand(policy, requestStore));
        registerRequiredCommand("adminapprove", new AdminApproveCommand(requestStore));
        registerRequiredCommand("adminreject", new AdminRejectCommand(requestStore));
        registerRequiredCommand("adminrequests", new AdminRequestsCommand(requestStore));
    }

    private void registerRequiredCommand(String commandName, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = this.getCommand(commandName);
        if (command == null) {
            throw new IllegalStateException("Command not defined in plugin.yml: " + commandName);
        }
        command.setExecutor(executor);
    }
}
