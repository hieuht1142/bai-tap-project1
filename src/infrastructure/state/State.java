package infrastructure.state;

import config.Constant;
import events.BLeavingSourceQueueEvent;
import events.CLeavingEXBEvent;
import events.FLeavingSwitchEvent;
import events.IEventGenerator;
import infrastructure.element.Element;
import infrastructure.entity.Node;
import infrastructure.event.Event;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.SourceQueue;
import network.entities.Host;
import network.entities.Switch;
import simulator.DiscreteEventSimulator;

public class State {
	public static int countPacket = 0;
	public static int countStateENB = 0;
	public static int countStateEXB = 0;	
	public IEventGenerator element;
	public Type type = Type.NONE;
	
	/**
	 * This method is used to set action for states
	 */
	public void act() {
		DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
		switch (type) {
			case X00:
				ExitBuffer exitBuffer = (ExitBuffer)this.element;
		        Node currentNode = exitBuffer.getNode();
		        handleCurrentNode(currentNode, sim, exitBuffer);
				break;			
			case X01:
				ExitBuffer exitBuffer1 = (ExitBuffer) this.element;
		        Node currentNode1 = exitBuffer1.getNode();
		        handleCurrentNode(currentNode1, sim, exitBuffer1);

		        Packet packet = exitBuffer1.getPeekPacket();
		        handlePacket(packet, exitBuffer1);
		        break;	        
			case X11:
				ExitBuffer exitBuffer2 = (ExitBuffer)this.element;
		        Packet packet2 = exitBuffer2.getPeekPacket();
		        handlePacket(packet2, exitBuffer2);
				break;
			default:
				break;
		}	
	}
	
	/**
	 * This method is used to handle event if current node is source node or switch
	 * @param currentNode current node
	 * @param sim simulator
	 * @param exitBuffer exit buffer
	 */
	private void handleCurrentNode(Node currentNode, DiscreteEventSimulator sim, ExitBuffer exitBuffer) {
		if (currentNode.isSourceNode()) {
			Host sourceNode = (Host)currentNode;
			SourceQueue sourceQueue = sourceNode.physicalLayer.sourceQueue;
			Packet packet = sourceQueue.getPeekPacket();
			if (packet != null) {
				if (!sourceQueue.hasEventOfPacket(packet)) {
					long time = (long)sourceQueue.physicalLayer.simulator.time();
					Event event = new BLeavingSourceQueueEvent(
							sim,
							time, time, sourceQueue, packet);
					event.register(); // insert new event
				}
			}
		} else if (currentNode instanceof Switch) {
			Switch sw = (Switch)currentNode;
			exitBuffer.getNode().getNetworkLayer().controlFlow(exitBuffer);
		}
	}
	
	/**
	 * This method is used to handle event if the packet is not null
	 * @param packet the packet
	 * @param exitBuffer exit buffer
	 */
	private void handlePacket(Packet packet, ExitBuffer exitBuffer) {
		if (packet != null) {
            if (!(exitBuffer.hasEventOfPacket(packet))) {
            	if (exitBuffer.getNode().isSourceNode()) {
            		long time = (long) exitBuffer.physicalLayer.simulator.time();
            		Event event = new CLeavingEXBEvent(
            				exitBuffer.physicalLayer.simulator,
            				time, time, exitBuffer, packet);
            		event.register(); // insert new event
            	} else if (exitBuffer.getNode() instanceof Switch) {
            		long time = (long) exitBuffer.physicalLayer.simulator.time();
            		Event event = new FLeavingSwitchEvent(
            				exitBuffer.physicalLayer.simulator,
            				time, time + Constant.SWITCH_CYCLE, exitBuffer, packet);
            		event.register(); // insert new event
            	}
            }
		}
	}
	
	
	public void getNextState(Element e) {
	}

}

