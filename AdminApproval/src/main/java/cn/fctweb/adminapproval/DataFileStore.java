package cn.fctweb.adminapproval;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DataFileStore {
    private final Path dataFile;

    public DataFileStore(Path dataFile) {
        this.dataFile = dataFile;
    }

    public StoreSnapshot load() {
        if (!Files.exists(this.dataFile)) {
            return new StoreSnapshot(1, List.of(), List.of());
        }

        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));

        try (InputStream input = Files.newInputStream(this.dataFile)) {
            Object loaded = yaml.load(input);
            if (!(loaded instanceof Map<?, ?> root)) {
                return new StoreSnapshot(1, List.of(), List.of());
            }

            int nextId = readInt(root.get("next-id"), 1);
            List<ApprovalRequest> pending = parsePending(root.get("pending"));
            List<ApprovalHistoryEntry> history = parseHistory(root.get("history"));
            return new StoreSnapshot(nextId, pending, history);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load " + this.dataFile, ex);
        }
    }

    public void save(StoreSnapshot snapshot) {
        try {
            Files.createDirectories(this.dataFile.getParent());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create data folder", ex);
        }

        Map<String, Object> root = new HashMap<>();
        root.put("next-id", snapshot.nextId());

        List<Map<String, Object>> pending = new ArrayList<>();
        for (ApprovalRequest request : snapshot.pending()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", request.id());
            item.put("requester-id", request.requesterId().toString());
            item.put("requester-name", request.requesterName());
            item.put("command", request.command());
            item.put("created-at", request.createdAt().toString());
            pending.add(item);
        }

        List<Map<String, Object>> history = new ArrayList<>();
        for (ApprovalHistoryEntry entry : snapshot.history()) {
            Map<String, Object> item = new HashMap<>();
            item.put("request-id", entry.requestId());
            item.put("requester-name", entry.requesterName());
            item.put("command", entry.command());
            item.put("reviewer-name", entry.reviewerName());
            item.put("action", entry.action());
            item.put("execution-success", entry.executionSuccess());
            item.put("processed-at", entry.processedAt().toString());
            history.add(item);
        }

        root.put("pending", pending);
        root.put("history", history);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        Yaml yaml = new Yaml(dumperOptions);
        try (Writer writer = Files.newBufferedWriter(this.dataFile)) {
            yaml.dump(root, writer);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save " + this.dataFile, ex);
        }
    }

    private List<ApprovalRequest> parsePending(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<ApprovalRequest> pending = new ArrayList<>();
        for (Object row : list) {
            if (!(row instanceof Map<?, ?> map)) {
                continue;
            }
            try {
                int id = readInt(map.get("id"), -1);
                UUID requesterId = UUID.fromString(readString(map.get("requester-id")));
                String requesterName = readString(map.get("requester-name"));
                String command = readString(map.get("command"));
                Instant createdAt = Instant.parse(readString(map.get("created-at")));
                if (id > 0) {
                    pending.add(new ApprovalRequest(id, requesterId, requesterName, command, createdAt));
                }
            } catch (Exception ignored) {
            }
        }
        return pending;
    }

    private List<ApprovalHistoryEntry> parseHistory(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<ApprovalHistoryEntry> history = new ArrayList<>();
        for (Object row : list) {
            if (!(row instanceof Map<?, ?> map)) {
                continue;
            }
            try {
                int requestId = readInt(map.get("request-id"), -1);
                String requesterName = readString(map.get("requester-name"));
                String command = readString(map.get("command"));
                String reviewerName = readString(map.get("reviewer-name"));
                String action = readString(map.get("action"));
                boolean executionSuccess = readBoolean(map.get("execution-success"));
                Instant processedAt = Instant.parse(readString(map.get("processed-at")));
                if (requestId > 0) {
                    history.add(new ApprovalHistoryEntry(requestId, requesterName, command, reviewerName, action,
                            executionSuccess, processedAt));
                }
            } catch (Exception ignored) {
            }
        }
        return history;
    }

    private int readInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private boolean readBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return false;
    }

    private String readString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
