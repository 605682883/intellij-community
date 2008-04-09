package com.intellij.slicer;

import com.intellij.util.ui.Tree;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

/**
 * @author cdr
 */
public class DuplicateNodeRenderer {
  public static interface DuplicatableNode {
    //returns first duplicate node, if any, or null if there are none
    //duplicate nodes are painted gray
    @Nullable
    AbstractTreeNode getDuplicate();
  }

  public static void paintDuplicateNodesBackground(Graphics g, Tree tree) {
    Rectangle clipBounds = g.getClipBounds();
    int start = tree.getClosestRowForLocation(clipBounds.x, clipBounds.y);
    int end = Math.min(tree.getRowCount(), tree.getClosestRowForLocation(clipBounds.x+clipBounds.width, clipBounds.y+clipBounds.height)+1);
    Color old = g.getColor();
    for (int i = start; i < end; i++) {
      TreePath path = tree.getPathForRow(i);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      Rectangle accumRect = null;
      TreePath accumPath = null;
      while (node != null) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof DuplicatableNode)) break;
        DuplicatableNode duplicatableNode = (DuplicatableNode)userObject;
        if (duplicatableNode.getDuplicate() == null) break;
        accumPath = accumRect == null ? path : accumPath.getParentPath();
        accumRect = tree.getPathBounds(accumPath).union(accumRect == null ? new Rectangle() : accumRect);
        node = (DefaultMutableTreeNode)node.getParent();
      }
      if (accumRect != null) {
        Rectangle rowRect = tree.getRowBounds(tree.getRowForPath(accumPath));
        accumRect = accumRect.intersection(new Rectangle(rowRect.x, rowRect.y, Integer.MAX_VALUE, Integer.MAX_VALUE));

        //unite all expanded children node rectangles since they can stretch out of parent's
        node = (DefaultMutableTreeNode)accumPath.getLastPathComponent();
        accumRect = accumRect.union(getExpandedNodesRect(tree, node, accumPath));

        g.setColor(new Color(230, 230, 230));
        g.fillRoundRect(accumRect.x, accumRect.y, accumRect.width, accumRect.height, 10, 10);
        g.setColor(Color.lightGray);
        g.drawRoundRect(accumRect.x, accumRect.y, accumRect.width, accumRect.height, 10, 10);
      }
    }
    g.setColor(old);
  }

  private static Rectangle getExpandedNodesRect(Tree tree, DefaultMutableTreeNode node, TreePath path) {
    Rectangle rect = tree.getRowBounds(tree.getRowForPath(path));
    if (tree.isExpanded(path)) {
      Enumeration<DefaultMutableTreeNode> children = node.children();
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child = children.nextElement();
        TreePath childPath = path.pathByAddingChild(child);
        rect = rect.union(getExpandedNodesRect(tree, child, childPath));
      }
    }
    return rect;
  }

}
