package custom.fattree;

import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dandoh on 5/24/17.
 */
public class FatTreeGraph extends Graph {
    public static final int CORE = 1;
    public static final int AGG  = 2;
    public static final int EDGE = 3;
    private final int numServers;
    private final int numPodSwitches;
    private final int numCores;
    private final int k;
    private Address[] address;

    private List<Integer> switches;
    private List<Integer> hosts;

    public FatTreeGraph(int k) {
        if (k % 2 == 1) throw new IllegalArgumentException("K must be even");
        if (k > 256) throw new IllegalArgumentException("K must less than 256");

        this.k = k;
        this.numServers = k * k * k / 4;
        this.numPodSwitches = k * k;
        this.numCores = k * k / 4;

        this.V = numServers + numPodSwitches + numCores;
        this.E = 0;

        adj = new List[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new ArrayList<Integer>();
        }
        
        buildEdge();
        
        buildAddress();
    }
    
    /**
     * This method is used to create edges between hosts and edge switches,
     * between edge switches and aggregation switches, between aggregation
     * switches and core switches of the fat-tree graph.
     */
    private void buildEdge() {
        // each pod has k^2/4 servers and k switches
        int numEachPod = k * k / 4 + k;
        for (int p = 0; p < k; p++) {
            int offset = numEachPod * p;
            
            buildServerEdge(offset);
            buildEdgeAgg(offset);
            buildAggCore(offset);
        }
    }
    
    /**
     * This method is used to create edges between server and edge switch of the fat-tree graph.
     *
     * @param offset the offset of each node in the fat-tree graph
     */
    private void buildServerEdge(int offset) {
    	for (int e = 0; e < k / 2; e++) { // each pod has k/2 edge switches
            int edgeSwitch = offset + k * k / 4 + e; // edgeSwitch denotes id of edge switches
            for (int s = 0; s < k / 2; s++) { // each edge switch connects to k/2 server
                int server = offset + e * k / 2 + s; // server denotes id of servers
                addEdge(edgeSwitch, server);
            }
        }
    }
    
    /**
     * This method is used to create edges between edge switch and aggregation switch of the fat-tree graph.
     *
     * @param offset the offset of each node in the fat-tree graph
     */
    private void buildEdgeAgg(int offset) {
    	for (int e = 0; e < k / 2; e++) { // each pod has k/2 edge switches
            int edgeSwitch = offset + k * k / 4 + e; // edgeSwitch denotes id of edge switches
            for (int a = k / 2; a < k; a++) { // each pod has k/2 aggregation switches
                int aggSwitch = offset + k * k / 4 + a; // aggSwitch denotes id of aggregation switches
                addEdge(edgeSwitch, aggSwitch);
            }
        }  
    }
    
    /**
     * This method is used to create edges between aggregation switch and core switch of the fat-tree graph.
     *
     * @param offset the offset of each node in the fat-tree graph
     */
    private void buildAggCore(int offset) {
    	for (int a = 0; a < k / 2; a++) { // each pod has k/2 aggregation switches
            int aggSwitch = offset + k * k / 4 + k / 2 + a; // aggSwitch denotes id of aggregation switches
            for (int c = 0; c < k / 2; c++) { // each aggregation switch connects to k/2 core switch
                int coreSwitch = a * k / 2 + c + numPodSwitches + numServers; // coreSwitch denotes id of core switches
                addEdge(aggSwitch, coreSwitch);
            }
        }
    }

    
    /**
     * This method is used to build address for all switches and hosts of the fat-tree topology
     */
    private void buildAddress() {
        this.address = new Address[V];

        int numEachPod = k * k / 4 + k;

        buildPodAddress(numEachPod);
        buildCoreAddress();
        buildHostAddress(numEachPod);
    }
    
    
    /**
     * This method is used to build address for pod's switches
     *
     * @param numEachPod the number of nodes in each pod
     */
    private void buildPodAddress(int numEachPod) {
    	// Form of pod switches addresses: 10.p.s.1
    	for (int p = 0; p < k; p++) { // p denotes the pod number
            int offset = numEachPod * p;  
            for (int s = 0; s < k; s++) { // s denotes the position of the switch in the pod
                int switchId = offset + k * k / 4 + s; 
                address[switchId] = new Address(10, p, s, 1);
            }
        }
    }
    
    /**
     * This method is used to build address for core switches
     */
    private void buildCoreAddress() {	
    	// Form of core switches addresses: 10.k.j.i
    	for (int j = 1; j <= k / 2; j++) {          // i, j denote the switch’s coordinates in the core switch grid
            for (int i = 1; i <= k / 2; i++) {      // starting from top-left
                int offset = numPodSwitches + numServers;
                int switchId = offset + (j - 1) * k / 2 + i - 1;
                address[switchId] = new Address(10, k, j, i);
            }
        }
    }
    
    /**
     * This method is used to build address for hosts
     *
     * @param numEachPod the number of nodes in each pod
     */
    private void buildHostAddress(int numEachPod) {
    	// Form of hosts addresses: 10.p.e.h
    	for (int p = 0; p < k; p++) { // p denotes the pod number
            int offset = numEachPod * p;
            for (int e = 0; e < k / 2; e++) { // e denotes the position of the switch in the pod
                for (int h = 2; h <= k / 2 + 1; h++) { // h is the host’s position in subnet 
                    int hostId = offset + e * k / 2 + h - 2;
                    address[hostId] = new Address(10, p, e, h);
                }
            }
        }
    }

    public int getK() {
        return k;
    }

    public Address getAddress(int u) {
        return address[u];
    }

    @Override
    public List<Integer> hosts() {
        if (hosts != null) return hosts;

        hosts = new ArrayList<>();

        int numEachPod = k * k / 4 + k;
        for (int p = 0; p < k; p++) {
            int offset = numEachPod * p;
            for (int e = 0; e < k / 2; e++) {
                for (int h = 2; h <= k / 2 + 1; h++) {
                    int serverId = offset + e * k / 2 + h - 2;
                    hosts.add(serverId);
                }
            }
        }

        return hosts;
    }

    @Override
    public List<Integer> switches() {
        if (switches != null) return switches;
        switches = new ArrayList<>();

        // add pod's switches
        int numEachPod = k * k / 4 + k;
        for (int p = 0; p < k; p++) {
            int offset = numEachPod * p;
            for (int s = 0; s < k; s++) {
                int switchId = offset + k * k / 4 + s;
                switches.add(switchId);
            }
        }

        // add core switches
        for (int j = 1; j <= k / 2; j++) {
            for (int i = 1; i <= k / 2; i++) {
                int offset = numPodSwitches + numServers;
                int switchId = offset + (j - 1) * k / 2 + i - 1;
                switches.add(switchId);
            }
        }

        return switches;
    }

    public boolean isHostVertex(int u) {
        if (u >= numServers + numPodSwitches) return false;
        int offset = u % (k * k / 4 + k);
        return offset < k * k / 4;
    }

    public boolean isSwitchVertex(int u) {
        return !isHostVertex(u);
    }

    public int switchType(int u) {
        int numEachPod = k * k / 4 + k;
        if (u >= k * numEachPod) return CORE;
        else {
            int os = u % numEachPod;
            if (os >= k * k / 4 + k / 2) return AGG;
            else return EDGE;
        }
    }
}
