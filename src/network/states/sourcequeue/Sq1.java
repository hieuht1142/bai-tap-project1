package network.states.sourcequeue;

import infrastructure.event.Event;
import events.AGenerationEvent;
import network.elements.SourceQueue;
import infrastructure.state.State;

public class Sq1 extends State {
	// State Sq1: source queue is empty.
	public Sq1(SourceQueue e)
	{
		this.element = e;
	}
	
	/**
	 * This method is called when an element's state is changed.
	 * Here, when the Source queue element is in the Sq1 state, 
	 * it checks to see if there is the event in the list of 
	 * impending events that generates the next packet. 
	 * If not, this event will be created.
	 * The timing of this event is the future (one more Constant.HOST_DELAY).
	 */
	@Override
	public void act() {
		SourceQueue sourceQueue = (SourceQueue) element;
		long time = (long)sourceQueue.getNextPacketTime();
		Event event = new AGenerationEvent(
				sourceQueue.physicalLayer.simulator,
				time, time, element);
		event.register();
	}
	

}
