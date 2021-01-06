package custom.fattree;

import javatuples.* ;
import network.elements.Packet;
import routing.RoutingAlgorithm;
import routing.RoutingPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import infrastructure.entity.Node;


/**
 * Created by Dandoh on 5/24/17.
 */
public class FatTreeRoutingAlgorithm implements RoutingAlgorithm, Cloneable {
    
	public FatTreeGraph G;
    
	public Map<Pair<Integer, Integer>, RoutingPath> precomputedPaths = new HashMap<>();
    
	public Map<Integer, Map<Integer, Integer>> suffixTables = new HashMap<>();
    
	public Map<Integer, Map<Integer, Integer>> getSuffixTables() {
		return suffixTables;
	}

	public void setSuffixTables(Map<Integer, Map<Integer, Integer>> suffixTables) {
		this.suffixTables = suffixTables;
	}

	private Map<Integer, Map<Triplet<Integer, Integer, Integer>, Integer>> prefixTables = new HashMap<>();
	
	public void setCorePrefixTables(Map<Integer, Map<Pair<Integer, Integer>, Integer>> corePrefixTables) {
		this.corePrefixTables = corePrefixTables;
	}

	public Map<Integer, Map<Triplet<Integer, Integer, Integer>, Integer>> getPrefixTables() {
		return prefixTables;
	}

	public void setPrefixTables(Map<Integer, Map<Triplet<Integer, Integer, Integer>, Integer>> prefixTables) {
		this.prefixTables = prefixTables;
	}

	private Map<Integer, Map<Pair<Integer, Integer>, Integer>> corePrefixTables = new HashMap<>();

	public FatTreeRoutingAlgorithm(FatTreeGraph G, boolean precomputed) {
		this.G = G;
		buildTables();
		if (precomputed) {
			List<Integer> hosts = G.hosts();
			for (int i = 0; i < hosts.size() - 1; i++) {
				for (int j = i + 1; j < hosts.size(); j++) {
					int source = hosts.get(i);
					int destination = hosts.get(j);
					path(source, destination);
				}
			}
		}
	}
	
	
	/**
	 * This method is used to build prefix - suffix routing tables
	 * for edge switches, aggregation switches and core switches
	 */
	private void buildTables() {
		int k = G.getK();
		int numEachPod = k * k / 4 + k;
        
		buildEdgeTable(k, numEachPod);
		buildAggTable(k, numEachPod);
		buildCoreTable(k, numEachPod);       
	}
    
	/**
	 * This method is used to build suffix routing tables for edge switches
	 *
	 * @param k the constant k in k-ary fat-tree
	 * @param numEachPod the number of nodes of each pod in the fat-tree topology
	 */
	private void buildEdgeTable(int k, int numEachPod) {
		for (int p = 0; p < k; p++) { // There are k pod in fat-tree topology
			int offset = numEachPod * p;
			for (int e = 0; e < k / 2; e++) {
				int edgeSwitch = offset + k * k / 4 + e; // edgeSwitch denotes id of edge switches
                
				// create suffix table
				HashMap<Integer, Integer> suffixTable = new HashMap<>();
				for (int suffix = 2; suffix <= k / 2 + 1; suffix++) {
					// add suffix 0.0.0.suffix/8 and port (e + suffix - 2) % (k / 2) for each edge switch
					int agg = offset + k * k / 4 + (e + suffix - 2) % (k / 2) + (k / 2);
					suffixTable.put(suffix, agg);
				}
				suffixTables.put(edgeSwitch, suffixTable);
			}
		}
	}
    
	/**
	 * This method is used to build prefix - suffix routing tables for edge switches
	 *
	 * @param k the constant k in k-ary fat-tree
	 * @param numEachPod the number of nodes in each pod in the fat-tree topology
	 */
	private void buildAggTable(int k, int numEachPod) {
		for (int p = 0; p < k; p++) {
			int offset = numEachPod * p;
			for (int a = 0; a < k / 2; a++) {
				int aggSwitch = offset + k * k / 4 + k / 2 + a;

				// create suffix table
				Map<Integer, Integer> suffixTable = new HashMap<>();
				for (int suffix = 2; suffix <= k / 2 + 1; suffix++) { 
					// add suffix 0.0.0.suffix/8 and port (suffix + a - 2) % (k / 2)  for each aggregation switch
					int core = a * k / 2 + (suffix + a - 2) % (k / 2) + numEachPod * k;
					suffixTable.put(suffix, core);
				}		
                
				suffixTables.put(aggSwitch, suffixTable);

				// create prefix table
				Map<javatuples.Triplet<Integer, Integer, Integer>, Integer> prefixTable = new HashMap<>();
				for (int e = 0; e < k / 2; e++) { // e denotes the subnet number
					// add prefix 10.p.e.0/24 and port e for each edge switch
					int edgeSwitch = offset + k * k / 4 + e;
					prefixTable.put(new javatuples.Triplet<>(10, p, e), edgeSwitch);
				}
                
				prefixTables.put(aggSwitch, prefixTable);
			}
		}
	}
    
	/**
	 * This method is used to build prefix routing tables for edge switches
	 *
	 * @param k the constant k in k-ary fat-tree
	 * @param numEachPod the number of nodes in each pod in the fat-tree topology
	 */
	private void buildCoreTable(int k, int numEachPod) {
		for (int c = 0; c < k * k / 4; c++) {
			int core = k * k * k / 4 + k * k + c;

			// create prefix table
			HashMap<Pair<Integer, Integer>, Integer> corePrefixTable = new HashMap<>();
			for (int p = 0; p < k; p++) { 
				// add prefix 10.p.0.0/16 and port p for core switch pointing to destination pod
				int offset = numEachPod * p;
				int agg = (c / (k / 2)) + k / 2 + k * k / 4 + offset;
				corePrefixTable.put(new Pair<>(10, p), agg);
			}
			corePrefixTables.put(core, corePrefixTable);
		}
	}

    
	/**
     * @param source the id of the source host
     * @param current the id of the current switch
     * @param destination the id of the destination host
     * @return the id of the next node which the packet will be forwarded to
     */
    @Override
    public int next(int source, int current, int destination) {
        if (G.isHostVertex(current)) {
            return G.adj(current).get(0);
        } else if (G.adj(current).contains(destination)) {
            return destination;
        } else {
            int type = G.switchType(current);
            
            if (type == FatTreeGraph.CORE) {
            	return nextCore(current, destination);
            } else if (type == FatTreeGraph.AGG) {
            	return nextAgg(current, destination);
            } else {
            	return nextEdge(current, destination);
            }
        }
    }
    
    /**
     * @param current the id of the current core switch
     * @param destination the id of the destination host
     * @return the id of the next node which the packet will be forwarded to
     */
    private int nextCore(int current, int destination) {
    	Address address = G.getAddress(destination);
        Pair<Integer, Integer> prefix = new Pair<>(address._1, address._2);
        Map<Pair<Integer, Integer>, Integer> corePrefixTable = corePrefixTables.get(current);

        return corePrefixTable.get(prefix);
    }
    
    /**
     * @param current the id of the the current aggregation switch
     * @param destination the id of the destination host
     * @return the id of the next node which the packet will be forwarded to
     */
    private int nextAgg(int current, int destination) {
    	Address address = G.getAddress(destination);

        Triplet<Integer, Integer, Integer> prefix = new Triplet<>(address._1, address._2, address._3);
        int suffix = address._4;

        Map<Triplet<Integer, Integer, Integer>, Integer> prefixTable = prefixTables.get(current);
        Map<Integer, Integer> suffixTable = suffixTables.get(current);

        if (prefixTable.containsKey(prefix)) {
            return prefixTable.get(prefix);
        } else {
            return suffixTable.get(suffix);
        }
    }
    
    /**
     * @param current the id of the current edge switch
     * @param destination the id of the destination host
     * @return the id of the next node which the packet will be forwarded to
     */
    private int nextEdge(int current, int destination) {
    	Address address = G.getAddress(destination);
        int suffix = address._4;

        Map<Integer, Integer> suffixTable = suffixTables.get(current);
        return suffixTable.get(suffix);
    }
    
    @Override
    public RoutingPath path(int source, int destination) {
    	return null;
    }
    
    public int next(Packet packet, Node node) {
    	return next(packet.getSource(), node.getId(), packet.getDestination());
    }
    
    public RoutingAlgorithm build(Node node) throws CloneNotSupportedException {
		return (RoutingAlgorithm) this.clone();
    }

    /**
     * This method is used to update packet
     */
    public void update(Packet p, Node node) {
    }

}
