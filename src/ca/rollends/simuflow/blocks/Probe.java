package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;

public class Probe extends Wire {

    public Probe(BasicSignal start, BasicSignal end, String name) {
        super(start, end);
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitProbe(this);
    }
}
