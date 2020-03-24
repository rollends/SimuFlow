package ca.rollends.simuflow.blocks.codegen.exceptions;

import ca.rollends.simuflow.blocks.BasicBlock;

public class AlgebraicLoopDetectedException extends SimuflowCompilationException {

    private final BasicBlock location;

    public AlgebraicLoopDetectedException(BasicBlock location) {
        this.location = location;
    }

    @Override
    public String getMessage() {
        return
            "Algebraic loop detected. Simuflow does not support resolving algebraic loops at runtime. One workaround is to introduce a fast, strictly proper first order system into the loop thereby making it algebraic and still yielding similar response dynamics for low frequencies.";
    }
}
