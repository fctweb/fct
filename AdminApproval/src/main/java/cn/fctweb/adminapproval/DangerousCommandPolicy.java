package cn.fctweb.adminapproval;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.util.Collections;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.List;
import java.util.Set;

public final class DangerousCommandPolicy {
    private static final Set<String> DANGEROUS_DEFAULT = Set.of(
            "op", "deop", "stop", "restart", "reload", "ban", "pardon", "whitelist", "give", "item", "execute"
    );
    private final Set<String> commandWhitelist;

    public DangerousCommandPolicy(Set<String> initialWhitelist) {
        this.commandWhitelist = Collections.synchronizedSet(new HashSet<>());
        if (initialWhitelist != null) {
            for (String entry : initialWhitelist) {
                String normalized = normalizeLabel(entry);
                if (!normalized.isEmpty()) {
                    this.commandWhitelist.add(normalized);
                }
            }
        }
    }

    public boolean isDangerous(String fullCommandLine) {
        String label = extractLabel(fullCommandLine);
        if (label == null || isInternalCommand(label)) {
            return false;
        }

        String normalizedInput = normalizeLabel(label);
        if (DANGEROUS_DEFAULT.contains(normalizedInput)) {
            return true;
        }

        String primary = resolvePrimaryLabel(label);
        return primary != null && DANGEROUS_DEFAULT.contains(primary);
    }

    public boolean requiresApproval(String fullCommandLine) {
        if (!isDangerous(fullCommandLine)) {
            return false;
        }
        String label = extractLabel(fullCommandLine);
        if (label == null) {
            return false;
        }
        String normalizedInput = normalizeLabel(label);
        if (isWhitelisted(normalizedInput)) {
            return false;
        }
        String primary = resolvePrimaryLabel(label);
        return primary == null || !isWhitelisted(primary);
    }

    public boolean addWhitelist(String commandLabel) {
        String normalized = normalizeLabel(commandLabel);
        if (normalized.isEmpty()) {
            return false;
        }
        return this.commandWhitelist.add(normalized);
    }

    public boolean removeWhitelist(String commandLabel) {
        String normalized = normalizeLabel(commandLabel);
        if (normalized.isEmpty()) {
            return false;
        }
        return this.commandWhitelist.remove(normalized);
    }

    public Set<String> getCommandWhitelist() {
        synchronized (this.commandWhitelist) {
            return Set.copyOf(this.commandWhitelist);
        }
    }

    public List<String> listWhitelist() {
        synchronized (this.commandWhitelist) {
            return this.commandWhitelist.stream().sorted().toList();
        }
    }

    private boolean isWhitelisted(String commandLabel) {
        if (commandLabel == null || commandLabel.isEmpty()) {
            return false;
        }
        return this.commandWhitelist.contains(commandLabel);
    }

    private boolean isInternalCommand(String label) {
        String normalized = normalizeLabel(label);
        return normalized.equals("adminrequest")
                || normalized.equals("adminapprove")
                || normalized.equals("adminreject")
                || normalized.equals("adminrequests")
                || normalized.equals("adminhistory")
                || normalized.equals("adminapproval");
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
        if (label == null) {
            return "";
        }
        String normalized = label.toLowerCase(Locale.ROOT);
        int separatorIndex = normalized.lastIndexOf(':');
        if (separatorIndex >= 0 && separatorIndex < normalized.length() - 1) {
            return normalized.substring(separatorIndex + 1);
        }
        return normalized.trim();
    }
}
