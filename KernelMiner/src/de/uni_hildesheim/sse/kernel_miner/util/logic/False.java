package de.uni_hildesheim.sse.kernel_miner.util.logic;

/**
 * The boolean constant "false".
 * 
 * @author Adam Krafczyk
 */
public final class False extends Formula {

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String toString() {
        return "0";
    }

}
