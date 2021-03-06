package events;

import infrastructure.element.Element;
import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.SourceQueue;
import network.states.sourcequeue.Sq1;
import network.states.sourcequeue.Sq2;
import simulator.DiscreteEventSimulator;

enum TypeB {
	B, B1, B2, B3, B4
}

public class BLeavingSourceQueueEvent extends Event {
	// BLeavingSourceQueueEvent represents the event type B: the packet leaving from Source Queue
	protected TypeB type = TypeB.B;
	
	public BLeavingSourceQueueEvent(DiscreteEventSimulator sim, long startTime, long endTime, Element elem, Packet p) {
		super(sim, endTime);
		this.startTime = startTime;
		this.endTime = endTime;
		this.element = elem;
		this.packet = p;
	}

	public TypeB getType() {
		return type;
	}

	public void setType(TypeB type) {
		this.type = type;
	}

	@Override
	public void actions() {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		SourceQueue sourceQueue = (SourceQueue)getElement();
		int connectedNodeID = sourceQueue.physicalLayer.links
				.get(sourceQueue.getId()).getOtherNode(sourceQueue.physicalLayer.node).getId();
		ExitBuffer exitBuffer = sourceQueue.physicalLayer.exitBuffers.get(connectedNodeID);
		if (((exitBuffer.getState().type == Type.X00) || (exitBuffer.getState().type == Type.X01))
				&& (sourceQueue.getState() instanceof Sq2 && sourceQueue.isPeekPacket(packet))) {			
			//change state source queue, type B1
			changeState(sourceQueue, exitBuffer);			
			//change state EXB,  type b4
			if (exitBuffer.isFull()) {
				handleFullExitBuffer(exitBuffer);
			}
			
			generateEvent(sourceQueue, exitBuffer, sim);
		}
	}
	
	@Override
	public void changeState(SourceQueue sourceQueue, ExitBuffer exitBuffer) {
		if (sourceQueue.hasOnlyOnePacket()) {
			sourceQueue.setState(new Sq1(sourceQueue));
		}
		sourceQueue.removePacket();
		exitBuffer.insertPacket(packet);			
		packet.setType(Type.P2); //change Packet state
	}
	
	/**
	 * This method is used to handle full exit buffer
	 * @param exitBuffer
	 */
	private void handleFullExitBuffer(ExitBuffer exitBuffer) {
		
		if (exitBuffer.getState().type == Type.X00) {
			exitBuffer.setType(Type.X10);
		}
		
		if (exitBuffer.getState().type == Type.X01) {
			exitBuffer.setType(Type.X11);
			exitBuffer.getState().act();
		}
	}
	
	@Override
	public void generateEvent(SourceQueue sourceQueue, ExitBuffer exitBuffer, DiscreteEventSimulator sim) {
		long time = (long)sourceQueue.physicalLayer.simulator.time();
		Event event = new CLeavingEXBEvent(sim, time, time, exitBuffer, packet);
		event.register(); // insert new event
	}
}
