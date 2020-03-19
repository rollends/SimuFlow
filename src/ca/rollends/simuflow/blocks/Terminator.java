package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.List;

public class Terminator extends SinkBlock {

    public Terminator() {
        super(List.of(new BasicSignal(Dimension.Any, BasicSignal.Type.REAL)));
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitSinkBlock(this);
    }

    @Override
    public Sequence initializationCode() {
        return new Sequence(List.of());
    }

    @Override
    public Sequence outputCode() {
        return new Sequence(List.of());
    }
}
