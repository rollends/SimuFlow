package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class NumpyOperations extends AbstractPythonOperationBuilder {
    private final Symbol npHstack = new Symbol("np.hstack");
    private final Symbol npVstack = new Symbol("np.vstack");

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
}
