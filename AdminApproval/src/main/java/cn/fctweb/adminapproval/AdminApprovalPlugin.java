package cn.fctweb.adminapproval;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class AdminApprovalPlugin extends JavaPlugin {
    private RequestStore requestStore;
    private DataFileStore dataFileStore;

    @Override
    public void onEnable() {
        DangerousCommandPolicy policy = new DangerousCommandPolicy();
        this.requestStore = new RequestStore(500);
        Path dataPath = this.getDataFolder().toPath().resolve("data.yml");
        this.dataFileStore = new DataFileStore(dataPath);

        loadData();
        this.requestStore.setSaveHook(this::saveData);

        this.getServer().getPluginManager().registerEvents(new DangerousCommandListener(policy), this);

        registerRequiredCommand("adminrequest", new AdminRequestCommand(policy, this.requestStore));
        registerRequiredCommand("adminapprove", new AdminApproveCommand(this.requestStore));
        registerRequiredCommand("adminreject", new AdminRejectCommand(this.requestStore));
        registerRequiredCommand("adminrequests", new AdminRequestsCommand(this.requestStore));
        registerRequiredCommand("adminhistory", new AdminHistoryCommand(this.requestStore));
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadData() {
        try {
            this.requestStore.load(this.dataFileStore.load());
        } catch (Exception ex) {
            this.getLogger().warning("无法加载 data.yml，已使用空数据启动: " + ex.getMessage());
            this.requestStore.load(new StoreSnapshot(1, java.util.List.of(), java.util.List.of()));
        }
    }

    private void saveData() {
        if (this.requestStore == null || this.dataFileStore == null) {
            return;
        }

        try {
            this.dataFileStore.save(this.requestStore.snapshot());
        } catch (Exception ex) {
            this.getLogger().warning("保存 data.yml 失败: " + ex.getMessage());
        }
    }

    private void registerRequiredCommand(String commandName, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = this.getCommand(commandName);
        if (command == null) {
            throw new IllegalStateException("Command not defined in plugin.yml: " + commandName);
        }
        command.setExecutor(executor);
    }
}
