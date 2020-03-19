package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.Sequence;

import java.util.*;

public class IntegrationStepCodeBuilder implements IBlockVisitor {
    private Sequence outputCode;
    private Set<BasicBlock> visitedSet;

    public IntegrationStepCodeBuilder() {
        visitedSet = new HashSet<>();
        outputCode = new Sequence(List.of());
    }

    public Sequence getResult() {
        return outputCode;
    }

    @Override
    public void visitSinkBlock(SinkBlock s) {
        visitedSet.add(s);

        // Should be only one input
        BasicSignal signal = s.getInputs().get(0);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }

    @Override
    public void visitSourceBlock(SourceBlock s) {
        // Done
    }

    @Override
    public void visitWire(Wire w) {
        visitedSet.add(w);

        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }

    @Override
    public void visitProbe(Probe w) {
        visitWire(w);
    }

    @Override
    public void visitNode(Node w) {
        visitedSet.add(w);

        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }

    @Override
    public void visitSum(Sum s) {
        visitedSet.add(s);

        // More than one input
        for(BasicSignal signal : s.getInputs()) {
            // Figure out source block
            BasicBlock source = signal.getSource();

            // Traverse
            if (!visitedSet.contains(source)) {
                source.accept(this);
            }
        }
    }

    @Override
    public void visitStatefulBlock(StatefulBlock s) {
        visitedSet.add(s);

        // Should be only one input
        BasicSignal signal = s.getInputs().get(0);

        // Generate Source Code
        Sequence code = s.integrationCode();
        outputCode = Sequence.concat(outputCode, code);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }
}
