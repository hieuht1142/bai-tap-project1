package events;

import infrastructure.event.Event;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.UnidirectionalWay;
import simulator.DiscreteEventSimulator;

public class EventHandler {
	private Packet packet;
	private ExitBuffer exitBuffer;
	private UnidirectionalWay unidirectionalWay;
	private DiscreteEventSimulator sim;
	
	public EventHandler(Packet packet, ExitBuffer exitBuffer, UnidirectionalWay unidirectionalWay,
			DiscreteEventSimulator sim) {
		this.packet = packet;
		this.exitBuffer = exitBuffer;
		this.unidirectionalWay = unidirectionalWay;
		this.sim = sim;
	}
	
	/**
	 * This method is used to insert new event type D
	 */
	public void addEventD() { 	
    	long time = (long)exitBuffer.physicalLayer.simulator.time();
        Event event = new DReachingENBEvent(
        		sim,
        		time,
                time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
                unidirectionalWay, packet);
        event.register(); // insert new event
    }
    
	/**
	 * This method is used to insert new event type G
	 */
	public void addEventG() {
    	long time = (long)exitBuffer.physicalLayer.simulator.time();
        Event event = new GReachingDestinationEvent(
        		sim,
        		time,
                time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
                unidirectionalWay, packet);
        event.register(); // insert new event
    }
}
