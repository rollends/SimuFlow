package ca.rollends.simuflow.blocks.python;

public abstract class AbstractSyntaxTree {
    public abstract void accept(IAbstractSyntaxTreeVisitor visitor);
}
