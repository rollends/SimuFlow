package ca.rollends.simuflow.blocks.python;

public class PlainStatement extends Statement {

    private final String stmt;

    public PlainStatement(String s) {
        stmt = s;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitPlainStatement(this);
    }

    @Override
    public String toString() {
        return stmt;
    }
}
