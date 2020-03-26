package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class NumpyOperations extends AbstractPythonOperationBuilder {
    private final Symbol npHstack = new Symbol("np.hstack");
    private final Symbol npVstack = new Symbol("np.vstack");
    private final Symbol npZeros = new Symbol("np.zeros");
    private final Symbol npSize = new Symbol("np.size");

    public Sequence imports() {
        return Sequence.from(
            new ImportStatement(new PlainExpression("numpy as np")),
            new ImportStatement(new PlainExpression("scipy as sp")),
            new ImportStatement(new PlainExpression("scipy.integrate"))
        );
    }

    public Expression hstack(TupleExpression exp) {
        return Call(npHstack, List.of(exp));
    }

    public Expression vstack(TupleExpression exp) {
        return Call(npVstack, List.of(exp));
    }

    public Expression zeros(TupleExpression exp) {
        return Call(npZeros, List.of(exp));
    }

    public Expression size(Expression exp) {
        return Call(npSize, List.of(exp));
    }

    public Expression size(Expression exp, Expression exp2) {
        return Call(npSize, List.of(exp, exp2));
    }
}
