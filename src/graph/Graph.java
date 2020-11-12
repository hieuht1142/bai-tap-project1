package graph;

import common.StdOut;

import java.util.*;

public abstract class Graph {
    protected int V; // number of vertexes (having index from 0 -> (V-1))
    protected int E; // number of edge
    protected List<Integer>[] adj; // list of vertexes which is the neighbor vertex

    /**
     * This method is used to create edge between vertex v and w
     * @param v vertex v
     * @param w vertex w
     */
    public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        E++;
        adj[v].add(w);
        adj[w].add(v);
    }
    
    /**
     * This method is used to delete edge between vertex v and w
     * @param v vertex v
     * @param w vertex w
     */
    public void removeEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        E--;
        adj[v].remove((Object)w);
        adj[w].remove((Object)v);
    }

    private void validateVertex(int v) { // vertexes have index from 0 -> (V-1)
        if (v < 0 || v >= V) {
        	throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
        }
    }

    public boolean hasEdge(int u, int v) {
        return adj[u].contains(v);
    }

    public int V() {
    	return V;
    }

    public List<Integer> adj(int v) { 
    	return adj[v]; 
    }

    public int degree(int u) {
        return adj[u].size();
    }

    public abstract List<Integer> hosts();

    public abstract List<Integer> switches();

    public abstract boolean isHostVertex(int v);

    public abstract boolean isSwitchVertex(int v);

    public double pathCableLength(List<Integer> path) {
        return 0;
    }

    /**
     * This method is used to find the shortest path between 2 vertexes
     * @param u the start vertex
     * @param v the destination vertex
     * @return list of vertexes on the shortest path
     */
    public List<Integer> shortestPath(int u, int v) {
        Queue<Integer> queue = new LinkedList<Integer>(); // queue of marked vertexes
        List<Integer> path = new ArrayList<>(); // list of vertexes on the shortest path
        boolean[] visited = new boolean[this.V]; // array of visited vertexes
        int[] trace = new int[this.V]; // array of previous visited vertexes on the path
        queue.add(u);
        visited[u] = true; // u is visited
        trace[u] = -1;
        startFinding(queue, path, trace, visited, v);
        return path;
    }
    
    private void startFinding(Queue<Integer> queue, List<Integer> path, int[] trace, boolean[] visited, int v) {
    	while(!queue.isEmpty()) {
            int uNode = queue.remove();
            if (uNode == v) { // uNode is destination vertex
                path.add(v);
                while(trace[v] != -1) {
                    v = trace[v];
                    path.add(v);
                }
                Collections.reverse(path);
                break;
            }
            for (int vNode: this.adj(uNode)) { // for each neighbor vertex of uNode
                if (!visited[vNode] && isSwitchVertex(vNode)) {
                    visited[vNode] = true;
                    trace[vNode] = uNode;
                    queue.add(vNode);
                }
            }
        }
    }

    /**
     * This method is used to find shortest paths from one vertex to every other vertexes
     * @param u the start vertex
     * @return Map of all shortest paths from vertex u to others.
     */
    public Map<Integer, List<Integer>> shortestPaths(int u) {
        Queue<Integer> queue = new LinkedList<Integer>();
        boolean[] visited = new boolean[this.V];
        int[] trace = new int[this.V]; // tree of shortest paths
        queue.add(u);
        visited[u] = true;
        trace[u] = -1;
        
        // find shortest paths from vertex u to others
        buildShortestPathsTree(queue, visited, trace);
        
        Map<Integer, List<Integer>> paths = new HashMap<>();
        for (int node : this.switches()) { // for each switch in graph
            List<Integer> path = getShortestPath(trace, node);
            paths.put(node, path);
        }
        return paths;
    }
    
    /**
     * This method is used to build tree of shortest paths from vertex u to others.
     */
    private void buildShortestPathsTree(Queue<Integer> queue, boolean[] visited, int[] trace) {
        while(!queue.isEmpty()) {
            int uNode = queue.remove();
            for (int vNode: this.adj(uNode)) {
                if (!visited[vNode] && isSwitchVertex(vNode)) {
                    visited[vNode] = true;
                    trace[vNode] = uNode; // trace is tree of shortest paths
                    queue.add(vNode);
                }
            }
        }
    }
    
    /**
     * This method is used to find shortest path from vertex u to node
     * @param node destination vertex
     * @return List of vertexes on the shortest path.
     */
    private List<Integer> getShortestPath(int[] trace, int node) {
    	List<Integer> path = new ArrayList<>(); // shortest path from node to vertex u
        int v = node;
        path.add(v);
        while (trace[v] != -1) {
            v = trace[v];
            path.add(v);
        }
        Collections.reverse(path); // shortest path from vertex u to node
        return path;
    }

    /**
     * This method is used to find all shortest paths to others vertexes of all vertexes in the graph
     * @return Map of all shortest paths
     */
    public Map<Integer, Map<Integer, List<Integer>>> allShortestPaths() {
        Map<Integer, Map<Integer, List<Integer>>> paths = new HashMap<>();
        Queue<Integer> queue = new LinkedList<Integer>();
        boolean[] visited = new boolean[this.V];
        int[] trace = new int[this.V];

        for (int u : switches()) { // for each switch in the graph
            queue.clear();
            Arrays.fill(visited, false);

            queue.add(u);
            visited[u] = true;
            trace[u] = -1;
            
            buildShortestPathsTree(queue, visited, trace); // find shortest paths from vertex u to others
            
            paths.put(u, new HashMap<>());
            for (int node : this.switches()) {
            	List<Integer> path = getShortestPath(trace, node); // get shortest path from vertex u to node
                paths.get(u).put(node, path);
            }
            StdOut.printf("Done for %d\n", u);
        }
        return paths;
    }

}
