package ca.rollends.simuflow.blocks.python;

public class ReturnStatement extends Statement {

    private final Expression rhs;

    public ReturnStatement(Expression rhs) {
        this.rhs = rhs;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitReturnStatement(this);
    }

    public Expression getRHS() { return rhs; }
}
