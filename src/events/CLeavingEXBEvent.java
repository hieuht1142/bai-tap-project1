package events;

import infrastructure.element.Element;
import infrastructure.entity.Node;
import infrastructure.event.Event;
import infrastructure.state.Type;
import network.elements.ExitBuffer;
import network.elements.Packet;
import network.elements.UnidirectionalWay;
import network.entities.Host;
import network.entities.Switch;
import network.entities.TypeOfHost;
import network.states.unidirectionalway.W0;
import network.states.unidirectionalway.W1;
import simulator.DiscreteEventSimulator;

public class CLeavingEXBEvent extends Event {
	// CLeavingEXBEvent represents the event type C: the packet leaving from EXB

    public CLeavingEXBEvent(DiscreteEventSimulator sim, long startTime, long endTime, Element elem, Packet p) {
    	super(sim, endTime);
        this.startTime = startTime;
        this.endTime = endTime;
        this.element = elem;
        this.packet = p;
    }

    @Override
    public void actions() {
    	DiscreteEventSimulator sim = DiscreteEventSimulator.getInstance();
    	ExitBuffer exitBuffer = (ExitBuffer)element;
        UnidirectionalWay unidirectionalWay = exitBuffer.physicalLayer.links.get(exitBuffer.physicalLayer.node.getId())
        									.getWayToOtherNode(exitBuffer.physicalLayer.node);        
        if(unidirectionalWay.getState() instanceof W0 && exitBuffer.isPeekPacket(packet)
                && ((exitBuffer.getState().type == Type.X11) || (exitBuffer.getState().type == Type.X01))) {        	
            unidirectionalWay.addPacket(exitBuffer.removePacket());
            
            changeState(exitBuffer, unidirectionalWay);           
            Node nextNode = unidirectionalWay.getToNode();
            
            if (nextNode instanceof Switch) { // next node is switch so add event D
            	generateEvent(packet, exitBuffer, sim, unidirectionalWay, 'D');           
            } else if (nextNode instanceof Host) {
            	Host h = (Host)nextNode;
            	if (h.type == TypeOfHost.Destination || h.type == TypeOfHost.Mix) {
            		generateEvent(packet, exitBuffer, sim, unidirectionalWay, 'G');
            	}
            }
        }
    }
    
    @Override
    public void changeState(ExitBuffer exitBuffer, UnidirectionalWay unidirectionalWay) {
    	// change Packet state
        packet.setType(Type.P3);
        
        //change EXB state
        exitBuffer.setType(Type.X00);
        exitBuffer.getState().act();
        
        //change uniWay state
        unidirectionalWay.setState(new W1(unidirectionalWay));
        unidirectionalWay.getState().act();
    }
    
}