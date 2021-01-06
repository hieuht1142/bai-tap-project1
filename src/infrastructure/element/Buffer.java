package infrastructure.element;

import common.Queue;
import network.elements.Packet;
import network.layers.PhysicalLayer;

public abstract class Buffer extends Element {
	protected Queue<Packet> allPackets;
	public PhysicalLayer physicalLayer;

	protected Buffer() {
		allPackets = new Queue<>();
	}
	
	/**
	 * 
	 * @param packet
	 * @return true if the packet is peak packet
	 */
	public boolean isPeekPacket(Packet packet){
		if(allPackets.isEmpty()) return false;
		return allPackets.peek() == packet;
	}
	
	/**
	 * 
	 * @return true if list of all packets is empty
	 */
	public boolean isEmpty(){
		return allPackets.isEmpty();
	}

	/**
	 * 
	 * @return peak packet
	 */
	public Packet getPeekPacket(){
		if(allPackets.isEmpty()) return null;
		return allPackets.peek();
	}

}
