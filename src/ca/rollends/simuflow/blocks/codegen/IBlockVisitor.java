package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.codegen.exceptions.AlgebraicLoopDetectedException;

public interface IBlockVisitor {
    void visitSinkBlock(SinkBlock s);
    void visitSourceBlock(SourceBlock s);
    void visitWire(Wire w);
    void visitProbe(Probe w);
    void visitNode(Node w);
    void visitSum(Sum s);
    void visitStatefulBlock(StatefulBlock s);
}
