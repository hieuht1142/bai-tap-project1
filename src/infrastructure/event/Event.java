package infrastructure.event;

import events.DReachingENBEvent;
import events.GReachingDestinationEvent;
import events.IEventGenerator;
import infrastructure.element.Element;
import network.elements.EntranceBuffer;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.SourceQueue;
import network.elements.UnidirectionalWay;
import simulator.DiscreteEventSimulator;

public abstract class Event extends umontreal.ssj.simevents.Event{
	protected Packet packet; // packet ID
	protected long startTime;
	protected long endTime;
	public static int countSubEvent = 0;

	protected IEventGenerator element;
		
	public Event(DiscreteEventSimulator sim, long time) {
		super(sim);
		this.eventTime = (double)time;
	}

	public Packet getPacket() {
		return packet;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public IEventGenerator getElement() {
		return element;
	}
	
	public void setPacket(Packet packet) {
		this.packet = packet;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setElement(Element element) {
		this.element = element;
	}
	
	public void register() {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		if(sim == null) return;
		sim.addEvent(this);
	}
	
	/**
	 * This method is used to create event type D or type G
	 * @param type create event type D if type = 'D', create event type G if type = 'G'
	 */
	public void generateEvent(Packet packet, ExitBuffer exitBuffer, 
			DiscreteEventSimulator sim, UnidirectionalWay unidirectionalWay, char type) {
		
		long time = (long)exitBuffer.physicalLayer.simulator.time();
		Event event = null;
		
		if (type == 'D') {
			event = new DReachingENBEvent(
					sim,
					time,
					time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
					unidirectionalWay, packet);
		} else if (type == 'G') {
			event = new GReachingDestinationEvent(
					sim,
					time,
					time + unidirectionalWay.getLink().getTotalLatency(packet.getSize()),
					unidirectionalWay, packet);
		}       
		event.register(); // insert new event
	}
	
	/**
	 * This method is used to create event type F
	 */
	public void generateEvent(ExitBuffer exitBuffer, DiscreteEventSimulator sim) {		
	}
	
	/**
	 * This method is used to create event type C
	 */
	public void generateEvent(SourceQueue sourceQueue, ExitBuffer exitBuffer, DiscreteEventSimulator sim) {		
	}
	
	/**
	 * This method is used to change state of packet, EXB and uniWay
	 * @param exitBuffer
	 * @param unidirectionalWay
	 */
	public void changeState(ExitBuffer exitBuffer, UnidirectionalWay unidirectionalWay) {	
	}
	
	/**
	 * This method is used to change state
	 * 
	 * @param entranceBuffer Entrance Buffer
	 * @param exitBuffer Exit Buffer
	 * @param sim DiscreteEventSimulator
	 */
	public void changeState(EntranceBuffer entranceBuffer, ExitBuffer exitBuffer, DiscreteEventSimulator sim) {		
	}
	
	/**
	 * This method is used to change state of EXB
	 * 
	 * @param unidirectionalWay
	 */
	public void changeState(UnidirectionalWay unidirectionalWay) {		
	}
	
	/**
	 * This method is used to change state of source queue
	 * @param sourceQueue
	 * @param exitBuffer
	 */
	public void changeState(SourceQueue sourceQueue, ExitBuffer exitBuffer) {		
	}
}
