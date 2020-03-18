package ca.rollends.simuflow.blocks.python;

public class Statement extends AbstractSyntaxTree {

    private final Symbol lhs;
    private final Expression rhs;

    public Statement(Symbol lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitStatement(this);
    }

    public Symbol getLHS() { return lhs; }
    public Expression getRHS() { return rhs; }
}
