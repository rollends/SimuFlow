package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.AbstractSyntaxTree;
import ca.rollends.simuflow.blocks.python.Program;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimuflowProgramBuilder  {

    private final BlockDiagram diagram;

    public SimuflowProgramBuilder(BlockDiagram diagram) {
        this.diagram = diagram;
    }

    public Program build() {
        Program prog = Program.newProgram();

        // Preparation Logic is just statements that are run before doing anything else
        for(BasicBlock b : diagram.getBlocks()) {
            prog = Program.newProgram(prog, b.makePreparationStep());
        }

        // Now we create the much needed simulation step function. First need to compute the output logic.
        BlockOutputCodeBuilder outputCodeBuilder = new BlockOutputCodeBuilder();
        diagram.accept(outputCodeBuilder);
        Sequence blockOutputCode = outputCodeBuilder.getResult();

        return prog;
    }

}
