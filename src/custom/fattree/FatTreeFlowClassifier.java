package custom.fattree;

import java.util.HashMap;
import java.util.Map;

import config.Constant;
import infrastructure.entity.Node;
import javatuples.Pair;
import javatuples.Triplet;
import network.elements.Packet;
import routing.RoutingAlgorithm;

public class FatTreeFlowClassifier extends FatTreeRoutingAlgorithm {
	
	public Map<Pair<Integer, Integer>, Long> flowSizesPerDuration = new HashMap<>();
	public Map<Integer, Long> outgoingTraffic = new HashMap<>();
	public Map<Pair<Integer, Integer>, Long> flowTable = new HashMap<>();
	private int currentNode;
	private int time = 0;
	
	public FatTreeFlowClassifier(FatTreeGraph G, boolean precomputed) {
		super(G, precomputed);
	}

	public int getCurrentNode() {
		return currentNode;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
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
			System.exit(0);
			return destination;
		} else {
			int type = G.switchType(current);
			if (type == FatTreeGraph.CORE) { // the current switch is core switch	
				return super.next(source, current, destination);      
			} else if (type == FatTreeGraph.AGG) { // the current switch is agg switch
                return nextAggSwitch(current, destination);
			} else { // the current switch is edge switch
				return nextEdgeSwitch(current, destination);
			}
		}
	}
	
	/**
	 * @param current the id of the the current aggregation switch
	 * @param destination the id of the destination host
	 * @return the id of the next node which the packet will be forwarded to
	 */
	private int nextAggSwitch(int current, int destination) {
		Address address = G.getAddress(destination);

		Triplet<Integer, Integer, Integer> prefix = new Triplet<>(address._1, address._2, address._3);
		int suffix = address._4;

		Map<Triplet<Integer, Integer, Integer>, Integer> prefixTable = getPrefixTables().get(current);
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
	private int nextEdgeSwitch(int current, int destination) {
		Address address = G.getAddress(destination);
		int suffix = address._4;

		Map<Integer, Integer> suffixTable = suffixTables.get(current);
		return suffixTable.get(suffix);
	}
	
	/**
	 * @param packet the current packet on link
	 * @param node the node which the current packet reaches
	 * @return the id of the next node which the packet will be forwarded to 
	 */
	@Override
	public int next(Packet packet, Node node) {
		int current = node.getId();
		int destination = packet.getDestination();
		int source = packet.getSource();
		
		if (G.isHostVertex(current)) {
			return G.adj(current).get(0);
		} else if (G.adj(current).contains(destination)) {
			return destination;
		} else {
			int type = G.switchType(current);
			if (type == FatTreeGraph.CORE) { // the current switch is core switch
				return super.next(source, current, destination); 
			} else if (type == FatTreeGraph.AGG) { // the current switch is agg switch
				return nextAggSwitch(current, destination);
			} else { // the current switch is edge switch
				return nextEdgeSwitch(current, destination);
			}
		}
	}

	@Override
	public RoutingAlgorithm build(Node node) throws CloneNotSupportedException {
		currentNode = node.getId();
		RoutingAlgorithm ra = super.build(node);
		if (ra instanceof FatTreeFlowClassifier) {
			FatTreeFlowClassifier ftfc = (FatTreeFlowClassifier)ra;
			ftfc.outgoingTraffic = new HashMap<>();
			ftfc.flowSizesPerDuration = new HashMap<>();
			ftfc.flowTable = new HashMap<>();
			return ftfc;
		}
		return ra;
	}
	
	@Override
	public void update(Packet p, Node node) {
		int src = p.getSource();
		int dst = p.getDestination();
		int currentTime = (int) node.physicalLayer.simulator.time();
		if (currentTime - time >= Constant.TIME_REARRANGE) {
			time = currentTime;
			// Update the result of the routing table
			flowSizesPerDuration = new HashMap<>();
		} else {
			Pair<Integer, Integer> flow = new Pair<>(src, dst);
			long value = p.getSize();
			if (flowSizesPerDuration.containsKey(flow)) {
				value += flowSizesPerDuration.get(flow);
			}
			flowSizesPerDuration.put(flow, value);
			value = p.getSize();
			int idNextNode = node.getId();
			if (outgoingTraffic.containsKey(idNextNode)) {
				value += outgoingTraffic.get(idNextNode);
			}
			outgoingTraffic.put(idNextNode, value);
		}
	}	
}
