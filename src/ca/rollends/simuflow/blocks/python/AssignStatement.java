package ca.rollends.simuflow.blocks.python;

public class AssignStatement extends Statement {

    private final Symbol lhs;
    private final Expression rhs;

    public AssignStatement(Symbol lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitAssignStatement(this);
    }

    public Symbol getLHS() { return lhs; }
    public Expression getRHS() { return rhs; }
}
