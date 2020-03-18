package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.python.Symbol;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.HashMap;
import java.util.Map;

public class BasicSignal {
    private final static Map<BasicSignal, BasicBlock> SignalSourceRegistry = new HashMap<>();
    private final String name;
    private final Dimension dimensions;
    private final Type type;

    public BasicSignal(Dimension dimensions, Type type) {
        this.dimensions = dimensions;
        this.type = type;
        this.name = String.format("signal_%d", hashCode());
    }

    public BasicSignal(Dimension dimensions, Type type, String name) {
        this.dimensions = dimensions;
        this.type = type;
        this.name = name;
    }

    public BasicBlock getSource() {
        return SignalSourceRegistry.get(this);
    }

    public void registerWith(BasicBlock block) {
        SignalSourceRegistry.put(this, block);
    }

    public enum Type {
        REAL,
    }

    public Symbol makeSymbol() {
        return new Symbol(name);
    }
}
