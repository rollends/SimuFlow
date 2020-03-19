package ca.rollends.simuflow.blocks.python;

public class PlainExpression extends Expression {
    private final String exp;

    public PlainExpression(String s) {
        this.exp = s;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitPlainExpression(this);
    }

    public String getCode() {
        return exp;
    }
}
