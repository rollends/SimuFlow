package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class TupleExpression extends Expression {

    private final List<Expression> elements;

    public TupleExpression(List<Expression> exprs) {
        this.elements = exprs;
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitTupleExpression(this);
    }
}
