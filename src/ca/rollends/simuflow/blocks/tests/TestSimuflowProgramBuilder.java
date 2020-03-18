package ca.rollends.simuflow.blocks.tests;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.codegen.SimuflowProgramBuilder;
import ca.rollends.simuflow.blocks.python.Program;
import ca.rollends.simuflow.blocks.python.PythonGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSimuflowProgramBuilder {

    @Test
    public void testBasicChain() {
        StepFunction source = new StepFunction(1.0, 0.0);
        TransferFunction tf = new TransferFunction(List.of(1.), List.of(1., 1.));
        Terminator sink = new Terminator();

        // Connect all of em up
        Wire w1 = new Wire(source.getOutputs().get(0), tf.getInputs().get(0));
        Wire w2 = new Wire(tf.getOutputs().get(0), sink.getInputs().get(0));

        // Create Diagram
        BlockDiagram diagram = new BlockDiagram(List.of(w1, tf, w2, source, sink));

        // Simuflow generation
        SimuflowProgramBuilder programBuilder = new SimuflowProgramBuilder(diagram);
        Program result = programBuilder.build();

        // Get resulting program
        PythonGenerator gen = new PythonGenerator();
        result.accept(gen);

        System.out.print(gen.toString());
    }
}
