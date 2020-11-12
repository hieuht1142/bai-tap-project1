package network.layers;


import network.elements.Packet;

public class DataLinkLayer extends Layer {
	public Packet packet;
	
	public DataLinkLayer(Packet p){
		this.packet = p;
	}

	/**
	 * This method is used to update packet information
	 * @param p packet
	 */
	public void update(Packet p) {
		
	}
}
