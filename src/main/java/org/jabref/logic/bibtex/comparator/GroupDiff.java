package org.jabref.logic.bibtex.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jabref.model.groups.GroupTreeNode;
import org.jabref.model.metadata.MetaData;

public class GroupDiff {
    private final GroupTreeNode originalGroupRoot;
    private final GroupTreeNode newGroupRoot;
    private final List<String> changes;

    GroupDiff(GroupTreeNode originalGroupRoot, GroupTreeNode newGroupRoot) {
        this.originalGroupRoot = originalGroupRoot;
        this.newGroupRoot = newGroupRoot;
        this.changes = new ArrayList<>();
        calculateDetailedDiff();
    }

    /**
     * Generates a detailed line-by-line diff of group structures.
     */
    private void calculateDetailedDiff() {
        String original = serializeGroups(originalGroupRoot);
        String modified = serializeGroups(newGroupRoot);

        if (!Objects.equals(original, modified)) {
            changes.addAll(generateUnifiedDiff(original, modified));
        }
    }

    /**
     * Serializes group hierarchy to indented text for comparison.
     */
    private String serializeGroups(GroupTreeNode root) {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb, 0);
        return sb.toString();
    }

    private void serializeHelper(GroupTreeNode node, StringBuilder sb, int depth) {
        sb.append("  ".repeat(depth))
          .append(node.getGroup().getName())
          .append("\n");
        node.getChildren().forEach(child ->
                serializeHelper(child, sb, depth + 1));
    }

    /**
     * Simple line-based diff
     */
    private List<String> generateUnifiedDiff(String original, String modified) {
        List<String> diffLines = new ArrayList<>();
        String[] oldLines = original.split("\n");
        String[] newLines = modified.split("\n");

        for (int i = 0; i < Math.max(oldLines.length, newLines.length); i++) {
            String oldLine = i < oldLines.length ? oldLines[i] : "";
            String newLine = i < newLines.length ? newLines[i] : "";

            if (!oldLine.equals(newLine)) {
                diffLines.add(String.format("- %s", oldLine));
                diffLines.add(String.format("+ %s", newLine));
            }
        }
        return diffLines;
    }

    /**
     * This method only detects whether a change took place or not. It does not determine the type of change. This would
     * be possible, but difficult to do properly, so we rather only report the change.
     */
    public static Optional<GroupDiff> compare(MetaData originalMetaData, MetaData newMetaData) {
        Optional<GroupTreeNode> originalGroups = originalMetaData.getGroups();
        Optional<GroupTreeNode> newGroups = newMetaData.getGroups();

        if (!Objects.equals(originalGroups, newGroups)) {
            return Optional.of(new GroupDiff(
                    originalGroups.orElse(null),
                    newGroups.orElse(null)
            ));
        }
        return Optional.empty();
    }

    public GroupTreeNode getOriginalGroupRoot() {
        return originalGroupRoot;
    }

    public GroupTreeNode getNewGroupRoot() {
        return newGroupRoot;
    }
}
