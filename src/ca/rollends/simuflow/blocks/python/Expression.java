package ca.rollends.simuflow.blocks.python;

public class Expression extends AbstractSyntaxTree {
    // TODO: Make a real expression class. Right now hacking the python expressions in.
    private final String exp;

    public Expression(String s) {
        this.exp = s;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitExpression(this);
    }

    public String getExpressionCode() {
        return exp;
    }
}
