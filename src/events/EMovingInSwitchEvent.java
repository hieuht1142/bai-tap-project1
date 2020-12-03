package events;

import config.Constant;
import infrastructure.element.Element;
import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.EntranceBuffer;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.entities.Switch;
import network.states.enb.N0;
import network.states.enb.N1;
import simulator.DiscreteEventSimulator;

enum TypeE {
	E, E1, E2
}

public class EMovingInSwitchEvent extends Event {
	// EMovingInSwitchEvent represents the event type E: the packet leaving from ENB of switch to EXB
	
	public TypeE type = TypeE.E;

	public EMovingInSwitchEvent(DiscreteEventSimulator sim, long startTime, long endTime, Element elem, Packet p) {
		super(sim, endTime);
		this.startTime = startTime;
		this.endTime = endTime;
		this.element = elem;
		this.packet = p;
	}
	
	@Override
	public void actions() {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		EntranceBuffer entranceBuffer = (EntranceBuffer) element;
		
		Switch sw = (Switch) entranceBuffer.physicalLayer.node;
		int nextNodeID = entranceBuffer.getNextNodeId();
		ExitBuffer exitBuffer = sw.physicalLayer.exitBuffers.get(nextNodeID);

		if (entranceBuffer.isPeekPacket(packet) && ((exitBuffer.getState().type == Type.X00) || (exitBuffer.getState().type == Type.X01))) {
			changeState(entranceBuffer, exitBuffer, sim);
		}
	}
	
	@Override
	public void changeState(EntranceBuffer entranceBuffer, ExitBuffer exitBuffer, DiscreteEventSimulator sim) {
		entranceBuffer.dropNextNode();
		entranceBuffer.removePacket();
		exitBuffer.insertPacket(packet);
		exitBuffer.removeFromRequestList(entranceBuffer);
		
		packet.setType(Type.P5); //change Packet state		
		if (entranceBuffer.getState() instanceof N1) {
			entranceBuffer.setState(new N0(entranceBuffer));
			entranceBuffer.getState().act();
		}
		if (exitBuffer.isFull()) {
			handleFullBuffer(exitBuffer);
		}
		if (exitBuffer.isPeekPacket(packet)) {
			generateEvent(exitBuffer, sim);
		}
		exitBuffer.getNode().getNetworkLayer().controlFlow(exitBuffer);
		if (!entranceBuffer.isEmpty()) {
			entranceBuffer.getNode().getNetworkLayer().route((entranceBuffer));
		}
	}
	
	/**
	 * This method is used change state of exit buffer if EXB is full
	 * 
	 * @param exitBuffer Exit Buffer
	 */
	private void handleFullBuffer(ExitBuffer exitBuffer) {
		type = TypeE.E2;
		if (exitBuffer.getState().type ==  Type.X00) {
			exitBuffer.setType(Type.X10);
			exitBuffer.getState().act();
		}
		if (exitBuffer.getState().type == Type.X01) {
			exitBuffer.setType(Type.X11);
			exitBuffer.getState().act();
		}
	}
	
	@Override
	public void generateEvent(ExitBuffer exitBuffer, DiscreteEventSimulator sim) {
		long time = (long)exitBuffer.physicalLayer.simulator.time();
		Event event = new FLeavingSwitchEvent(
				sim,
				time,
				time + Constant.SWITCH_CYCLE, exitBuffer, packet);
		event.register();
	}
}
