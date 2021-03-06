package events;

import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.SourceQueue;
import network.elements.Packet;
import network.states.sourcequeue.Sq1;
import network.states.sourcequeue.Sq2;
import simulator.DiscreteEventSimulator;

public class AGenerationEvent extends Event {
	// AGenerationEvent represents the event type A: the generated packet
	
	public AGenerationEvent(DiscreteEventSimulator sim, long startTime, long endTime, IEventGenerator elem) {
		super(sim, endTime);
		this.element = elem;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public void actions() {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		SourceQueue sourceQueue = (SourceQueue)getElement();		
		Packet newPacket = sourceQueue.generatePacket(this.getStartTime());
		if(newPacket == null) { return; }
		newPacket.setId(sim.numSent++);
		this.setPacket(newPacket);
		newPacket.setType(Type.P1);
		
		// update source queue state
		if(sourceQueue.getState() instanceof Sq1) { // it means that element is an instance of SourceQueue
			sourceQueue.setState(new Sq2(sourceQueue));
		}
		long time = (long)sim.time();
		Event event = new BLeavingSourceQueueEvent(sim, time, time, sourceQueue, newPacket);		
		event.register();
		
		time = (long)sourceQueue.getNextPacketTime();		
		Event ev = new AGenerationEvent(sim, time, time, sourceQueue);	
		ev.register();		
	}	
}
