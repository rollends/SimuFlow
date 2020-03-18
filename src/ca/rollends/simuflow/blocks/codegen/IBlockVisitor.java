package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.SinkBlock;
import ca.rollends.simuflow.blocks.SourceBlock;
import ca.rollends.simuflow.blocks.StatefulBlock;
import ca.rollends.simuflow.blocks.Wire;

public interface IBlockVisitor {
    void visitSinkBlock(SinkBlock s);
    void visitSourceBlock(SourceBlock s);
    void visitWire(Wire w);
    void visitStatefulBlock(StatefulBlock s);
}
