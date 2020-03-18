package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class Scope extends AbstractSyntaxTree {
    protected final Sequence operations;

    public Scope(Sequence seq) {
        operations = seq;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitScope(this);
    }

    public Sequence getImplementation() { return operations; }
}
