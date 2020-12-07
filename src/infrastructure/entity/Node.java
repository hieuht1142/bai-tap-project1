package infrastructure.entity;


import network.layers.*;
import routing.RoutingAlgorithm;
import simulator.DiscreteEventSimulator;

/**
 * Created by Dandoh on 6/27/17.
 */
public abstract class Node extends Device {
	
	private NetworkLayer networkLayer;
	public PhysicalLayer physicalLayer;
	public DataLinkLayer dataLinkLayer;
	
	/**
	 * This method is used to set network layer for device
	 * @param ra RoutingAlgorithm
	 * @param node
	 */
	public void setNetworkLayer(RoutingAlgorithm ra, Node node) {
		this.networkLayer = new NetworkLayer(ra, node);
	}
	
	public NetworkLayer getNetworkLayer() {
		return networkLayer;
	}

	/**
	 * This method is used to set network layer for device
	 * @param networkLayer
	 */
	public void setNetworkLayer(NetworkLayer networkLayer) {
		this.networkLayer = networkLayer;
	}
	
	public Node(int id) {
		super(id);
	}
    
	/**
	 * This method is used to set simulator for device
	 * @param sim simulator
	 */
	public void setSimulator(DiscreteEventSimulator sim) {
		physicalLayer.simulator = sim;
	}
    
	public boolean isDestinationNode() {
		return false;
	}
    
	public boolean isSourceNode() {
		return false;
	}
}
