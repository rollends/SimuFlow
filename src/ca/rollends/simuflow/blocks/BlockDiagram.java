package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;

import java.util.List;

public class BlockDiagram {
    private final List<BasicBlock> blocks;

    public BlockDiagram(List<BasicBlock> blocks) {
        this.blocks = blocks;
    }

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    public void accept(IBlockVisitor visitor) {
        for(BasicBlock b : blocks) {
            if(b instanceof SinkBlock) {
                b.accept(visitor);
            }
        }
    }
}
