package cn.fctweb.adminapproval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RequestStore {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<Integer, ApprovalRequest> pending = new ConcurrentHashMap<>();

    public ApprovalRequest create(UUID requesterId, String requesterName, String command) {
        int id = this.nextId.getAndIncrement();
        ApprovalRequest request = new ApprovalRequest(id, requesterId, requesterName, command, Instant.now());
        this.pending.put(id, request);
        return request;
    }

    public ApprovalRequest remove(int id) {
        return this.pending.remove(id);
    }

    public List<ApprovalRequest> list() {
        List<ApprovalRequest> result = new ArrayList<>(this.pending.values());
        result.sort(Comparator.comparingInt(ApprovalRequest::id));
        return result;
    }
}
