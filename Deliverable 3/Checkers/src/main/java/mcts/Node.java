package mcts;

import game.Move;

import java.util.HashMap;
import java.util.Map;

public class Node {

    public int visitCount;
    public double valueSum;
    public double prior;
    public int player; // -1 or 1

    public Map<Move, Node> children;

    public Node(double prior) {
        this.prior = prior;
        this.visitCount = 0;
        this.valueSum = 0.0;
        this.children = new HashMap<>();
    }

    public boolean isExpanded() {
        return !children.isEmpty();
    }

    public double getQ() {
        if (visitCount == 0) return 0.0;
        return valueSum / visitCount;
    }
}
