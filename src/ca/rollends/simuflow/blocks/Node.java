package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.AssignStatement;
import ca.rollends.simuflow.blocks.python.Expression;
import ca.rollends.simuflow.blocks.python.PlainExpression;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.LinkedList;
import java.util.List;

public class Node extends BasicBlock {

    public Node(int numOutputs) {
        super(List.of(new BasicSignal(Dimension.Any, BasicSignal.Type.REAL)), makeSignalList(numOutputs));
    }

    private static List<BasicSignal> makeSignalList(int n) {
        List<BasicSignal> list = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            list.add(new BasicSignal(Dimension.Any, BasicSignal.Type.REAL));
        }
        return list;
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public Sequence initializationCode() {
        return Sequence.empty();
    }

    @Override
    public Sequence outputCode() {
        BasicSignal in = getInputs().get(0);

        Sequence setOutputValues = Sequence.empty();
        for(BasicSignal out : getOutputs()) {
            AssignStatement assign = new AssignStatement(out.makeSymbol(), new PlainExpression(in.makeSymbol().toString()));
            setOutputValues = Sequence.concat(setOutputValues, Sequence.from(assign));
        }

        return setOutputValues;
    }
}
