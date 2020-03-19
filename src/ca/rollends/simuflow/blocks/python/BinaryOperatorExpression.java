package ca.rollends.simuflow.blocks.python;

public class BinaryOperatorExpression extends Expression {
    private final Expression lhs, rhs;
    private final String operator;

    public BinaryOperatorExpression(Expression lhs, String operator, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }

    public Expression getLeftOperand() {
        return lhs;
    }
    public Expression getRightOperand() {
        return rhs;
    }
    public String getOperator() {
        return operator;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitBinaryOperatorExpression(this);
    }
}
