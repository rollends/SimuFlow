package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class Sequence extends AbstractSyntaxTree {
    protected final List<AbstractSyntaxTree> operations;

    public Sequence(List<AbstractSyntaxTree> ops) {
        operations = ops;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitSequence(this);
    }

    public List<AbstractSyntaxTree> getChildren() { return operations; }
}
