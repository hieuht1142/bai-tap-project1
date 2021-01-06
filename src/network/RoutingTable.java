package network;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    private HashMap<Integer, Integer> table;

    public RoutingTable() {
        table = new HashMap<>();
    }

    public Map<Integer, Integer> getTable() {
        return table;
    }

    /**
     * This method is used to add routes for routing table
     * @param destination
     * @param nextHop
     */
    public void addRoute(int destination, int nextHop) {
        table.put(destination, nextHop);
    }

    public int getNextNode(int u) {
        return table.get(u);
    }

    /**
     * This method is used get size of the routing table
     * @return size of the routing table
     */
    public int size() {
        return table.size();
    }
}
