package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

public final class DangerousCommandPolicy {
    private static final Set<String> DANGEROUS = Set.of(
            "op", "deop", "give", "item", "gamemode", "tp", "teleport", "execute", "ban", "pardon", "whitelist", "stop", "reload"
    );

    public boolean isDangerous(String fullCommandLine) {
        String label = extractLabel(fullCommandLine);
        if (label == null || isInternalCommand(label)) {
            return false;
        }

        String normalizedInput = normalizeLabel(label);
        if (DANGEROUS.contains(normalizedInput)) {
            return true;
        }

        String primary = resolvePrimaryLabel(label);
        return primary != null && DANGEROUS.contains(primary);
    }

    private boolean isInternalCommand(String label) {
        String normalized = normalizeLabel(label);
        return normalized.equals("adminrequest")
                || normalized.equals("adminapprove")
                || normalized.equals("adminreject")
                || normalized.equals("adminrequests");
    }

    private String extractLabel(String fullCommandLine) {
        if (fullCommandLine == null) {
            return null;
        }

        String value = fullCommandLine.trim();
        if (value.isEmpty()) {
            return null;
        }

        if (value.charAt(0) == '/') {
            value = value.substring(1);
        }

        if (value.isEmpty()) {
            return null;
        }

        int firstSpace = value.indexOf(' ');
        if (firstSpace == -1) {
            return value;
        }
        return value.substring(0, firstSpace);
    }

    private String resolvePrimaryLabel(String label) {
        CommandMap commandMap = getCommandMap(Bukkit.getServer());
        if (commandMap == null) {
            return null;
        }

        String direct = label.toLowerCase(Locale.ROOT);
        Command command = commandMap.getCommand(direct);

        if (command == null) {
            command = commandMap.getCommand(normalizeLabel(label));
        }

        if (command == null) {
            return null;
        }

        return normalizeLabel(command.getName());
    }

    private CommandMap getCommandMap(Server server) {
        try {
            Method method = server.getClass().getMethod("getCommandMap");
            Object value = method.invoke(server);
            if (value instanceof CommandMap commandMap) {
                return commandMap;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }

    private String normalizeLabel(String label) {
        String normalized = label.toLowerCase(Locale.ROOT);
        int separatorIndex = normalized.lastIndexOf(':');
        if (separatorIndex >= 0 && separatorIndex < normalized.length() - 1) {
            return normalized.substring(separatorIndex + 1);
        }
        return normalized;
    }
}
