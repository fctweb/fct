package cn.fctweb.adminapproval;

import java.util.List;

public record StoreSnapshot(int nextId, List<ApprovalRequest> pending, List<ApprovalHistoryEntry> history) {
}
