package infrastructure.element;

import infrastructure.state.State;
import network.elements.Packet;
import simulator.DiscreteEventSimulator;
import infrastructure.state.*;

import events.IEventGenerator;

public abstract class Element implements IEventGenerator {
	protected int id;
	protected State state;
	protected long soonestEndTime = Long.MAX_VALUE;

	protected Element() {
		
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * This method is used to set type for state of element
	 * @param type type of state
	 */
	public void setType(Type type) {
		if(this.state == null) {
			this.state = new State();
		}
			
		this.state.type = type;
	}

	/**
	 * This method is used to set soonest end time for the element
	 * @param soonestEndTime
	 */
	public void setSoonestEndTime(long soonestEndTime) {
		this.soonestEndTime = soonestEndTime;
	}

	public int getId() {
		return id;
	}

	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}

	public long getSoonestEndTime() { 
		return soonestEndTime; 
	}

	/**
	 * 
	 * @param packet
	 * @return false if the packet has no event
	 */
	public boolean hasEventOfPacket(Packet packet) {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		if (sim == null) {
			return false;
		}
		
		return false;
	}
}
