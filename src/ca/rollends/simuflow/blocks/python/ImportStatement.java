package ca.rollends.simuflow.blocks.python;

public class ImportStatement extends Statement {

    private final Expression rhs;

    public ImportStatement(Expression rhs) {
        this.rhs = rhs;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitImportStatement(this);
    }

    public Expression getRHS() { return rhs; }
}