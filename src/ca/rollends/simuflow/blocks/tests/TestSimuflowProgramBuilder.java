package ca.rollends.simuflow.blocks.tests;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.codegen.SimuflowProgramBuilder;
import ca.rollends.simuflow.blocks.python.Program;
import ca.rollends.simuflow.blocks.python.PythonGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestSimuflowProgramBuilder {

    @Test
    public void testBasicChain() throws IOException {
        // This diagram gives the step response, of amplitude 10, with the BIBO stable,
        // first order transfer function 1/(s+1). The 2% settling time is 4 seconds.
        // So at the end of the time limit we better be near the DC gain
        // times 10.

        StepFunction source = new StepFunction(10.0, 0.0);
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
    }
}
