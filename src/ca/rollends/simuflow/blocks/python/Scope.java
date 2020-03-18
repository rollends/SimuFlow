package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class Scope extends AbstractSyntaxTree {
    protected final List<AbstractSyntaxTree> operations;

    public Scope(List<AbstractSyntaxTree> ops) {
        operations = ops;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitScope(this);
    }

    public List<AbstractSyntaxTree> getChildren() { return operations; }
}
