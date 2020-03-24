package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class FunctionCallStatement extends Statement {
    private final Symbol functionRef;
    private final List<Expression> arguments;

    public FunctionCallStatement(Symbol functionRef, List<Expression> arguments) {
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
        visitor.visitFunctionCallStatement(this);
    }
}