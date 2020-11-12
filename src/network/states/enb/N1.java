package network.states.enb;

import infrastructure.state.State;
import network.elements.EntranceBuffer;

public class N1 extends State {
	// State N1: ENB is full.
    public N1(EntranceBuffer entranceBuffer){
        this.element = entranceBuffer;
    }
    
    @Override
    public void act(){

    }
}
