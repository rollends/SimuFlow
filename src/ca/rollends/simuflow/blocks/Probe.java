package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;

public class Probe extends Wire {

    private final String name;

    public Probe(BasicSignal start, BasicSignal end, String name) {
        super(start, end);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitProbe(this);
    }
}
