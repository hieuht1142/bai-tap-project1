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
		if (allPackets.size() > size) { // buffer is full
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		}
			
		allPackets.enqueue(p);
	}
	
	/**
	 * This method is used to remove first packet from buffer
	 * @return the removed packet
	 */
	public Packet removePacket() {
		if (allPackets.isEmpty()) return null;
		return allPackets.dequeue();
	}
	
	/**
	 * This method is used to check if buffer is full
	 */
	public boolean isFull() {
		if (allPackets.size() > size)
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		return allPackets.size() == size;
	}

	/**
	 * This method is used to get number of packets in buffer
	 * @return number of packets in buffer
	 */
	public int getNumOfPacket() {
		if (allPackets.size() > size) {
			System.out.println("ERROR: Buffer: " + this.toString() + " oversized");
		}
			
		return allPackets.size();
	}

	/**
	 * This method is used to check if we can add a packet into buffer
	 */
	public boolean canAddPacket() {
		return allPackets.size() < size ;
	}
}
