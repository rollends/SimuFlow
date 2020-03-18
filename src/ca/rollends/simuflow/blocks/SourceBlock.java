package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.BasicBlock;
import ca.rollends.simuflow.blocks.BasicSignal;
import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;

import java.util.List;

public abstract class SourceBlock extends BasicBlock {

    protected SourceBlock(List<BasicSignal> outputs) {
        super(List.of(), outputs);
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitSourceBlock(this);
    }
}
