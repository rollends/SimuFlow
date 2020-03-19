package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.*;

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
    public Sequence initializationCode() {
        return Sequence.empty();
    }

    @Override
    public Sequence outputCode() {
        BasicSignal in = getInputs().get(0);
        BasicSignal out = getOutputs().get(0);
        return Sequence.from(new AssignStatement(out.makeSymbol(), new PlainExpression(in.makeSymbol().toString())));
    }
}
