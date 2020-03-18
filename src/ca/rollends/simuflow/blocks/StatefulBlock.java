package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;
import ca.rollends.simuflow.blocks.python.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public abstract class StatefulBlock extends BasicBlock {

    protected final Symbol initialStateVariable;
    protected final Symbol stateVariable;
    protected final Symbol dStateVariable;

    public StatefulBlock(List<BasicSignal> inputs, List<BasicSignal> outputs) {
        super(inputs, outputs);

        initialStateVariable = new Symbol(String.format("x0_%d", hashCode()));
        stateVariable = new Symbol(String.format("x_%d", hashCode()));
        dStateVariable = new Symbol(String.format("dx_%d", hashCode()));
    }

    public Symbol getStateVariable() {
        return stateVariable;
    }

    public Symbol getDifferentialVariable() {
        return dStateVariable;
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitStatefulBlock(this);
    }

    public abstract Sequence makeIntegrationStep();
}
