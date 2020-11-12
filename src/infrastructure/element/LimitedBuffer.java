package infrastructure.element;

import infrastructure.entity.Node;
import network.elements.Packet;

public abstract class LimitedBuffer extends Buffer {
	
	protected Node node;
	protected Node connectNode;
	protected int size;


	public Node getConnectNode() {
		return connectNode;
	}

	public Node getNode() {
		return node;
	}

	/**
	 * This method is used to insert packet p into its buffer
	 * @param p the inserted packet
	 * @return true if the packet is inserted,
	 *         false if the packet isn't inserted (which means the buffer is full)
	 */
	public void insertPacket(Packet p) {
		if (allPackets.size() > size) {
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		}
			
		allPackets.enqueue(p);
	}
	
	public Packet removePacket() {
		if (allPackets.isEmpty()) return null;
		return allPackets.dequeue();
	}
	
	public boolean isFull() {
		if (allPackets.size() > size)
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		return allPackets.size() == size;
	}

	public int getNumOfPacket() {
		if (allPackets.size() > size) {
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		}
			
		return allPackets.size();
	}

	public boolean canAddPacket() {
		return allPackets.size() < size ;
	}
}
