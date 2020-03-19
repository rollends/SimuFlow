package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class FunctionCallExpression extends Expression {
    private final Symbol functionRef;
    private final List<Expression> arguments;

    public FunctionCallExpression(Symbol functionRef, List<Expression> arguments) {
        this.functionRef = functionRef;
        this.arguments = arguments;
    }

    public Symbol getFunctionName() {
        return functionRef;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitFunctionCallExpression(this);
    }
}