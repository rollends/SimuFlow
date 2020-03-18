package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;

import java.util.List;

public abstract class SinkBlock extends BasicBlock {

    protected SinkBlock(List<BasicSignal> inputs) {
        super(inputs, List.of());
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitSinkBlock(this);
    }
}
