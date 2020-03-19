package ca.rollends.simuflow.blocks.python;

public class UnaryOperatorExpression extends Expression {
    private final Expression operand;
    private final String operator;

    public UnaryOperatorExpression(String operator, Expression operand) {
        this.operand = operand;
        this.operator = operator;
    }

    public Expression getOperand() {
        return operand;
    }
    public String getOperator() {
        return operator;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitUnaryOperatorExpression(this);
    }
}
