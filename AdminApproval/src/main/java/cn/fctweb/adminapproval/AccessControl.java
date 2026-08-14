package cn.fctweb.adminapproval;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public final class AccessControl {
    private final Set<UUID> ownerUuids;

    public AccessControl(Set<UUID> ownerUuids) {
        this.ownerUuids = ownerUuids == null ? Set.of() : Set.copyOf(ownerUuids);
    }

    public boolean isOwner(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        return isOwner(player);
    }

    public boolean isOwner(Player player) {
        return this.ownerUuids.contains(player.getUniqueId());
    }

    public boolean isAdmin(Player player) {
        return isOwner(player) || isOperator(player);
    }

    public Set<UUID> ownerUuids() {
        return this.ownerUuids;
    }

    private boolean isOperator(Player player) {
        try {
            Method method = player.getClass().getMethod("isOp");
            Object value = method.invoke(player);
            if (value instanceof Boolean bool) {
                return bool;
            }
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
        return false;
    }
}
