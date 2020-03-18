package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;

import java.util.List;

public class Wire extends BasicBlock {
    public Wire(BasicSignal start, BasicSignal end) {
        super(List.of(start), List.of(end));
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitWire(this);
    }

    @Override
    public Sequence makePreparationStep() {
        return new Sequence(List.of());
    }

    @Override
    public Sequence makeOutputStep() {
        return new Sequence(List.of());
    }
}
