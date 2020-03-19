package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockOutputCodeBuilder implements IBlockVisitor {
    private Sequence outputCode;
    private Set<BasicBlock> visitedSet;
    private Deque<BasicBlock> operationStack;

    public BlockOutputCodeBuilder() {
        operationStack = new LinkedList<>();
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

        // Push this block on the stack (Last to do anything)
        operationStack.push(s);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
        }
    }

    private void drainStack() {
        // Should have the stack full now, so generate output code.
        while(!operationStack.isEmpty()) {
            BasicBlock block = operationStack.poll();

            outputCode = Sequence.concat(outputCode, block.outputCode());
        }
    }

    @Override
    public void visitSourceBlock(SourceBlock s) {
        visitedSet.add(s);

        // Push this block on the stack
        operationStack.push(s);

        drainStack();
    }

    @Override
    public void visitWire(Wire w) {
        visitedSet.add(w);

        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(w);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
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

        // Push this block on the stack
        operationStack.push(w);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
        }
    }

    @Override
    public void visitSum(Sum s) {
        visitedSet.add(s);

        // Clean stack
        Deque<BasicBlock> oldStack = new LinkedList<>();
        oldStack.addAll(operationStack);
        operationStack.clear();

        // More than one input
        for(BasicSignal signal : s.getInputs()) {
            // Figure out source block
            BasicBlock source = signal.getSource();

            // Traverse
            if (!visitedSet.contains(source)) {
                source.accept(this);
            }

            drainStack();
        }

        operationStack.addAll(oldStack);
        operationStack.push(s);
        drainStack();
    }

    @Override
    public void visitStatefulBlock(StatefulBlock s) {
        visitedSet.add(s);

        // TODO: Right now assuming stateful block has only one input. Is this right?
        BasicSignal signal = s.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(s);


        if(!s.hasFeedforward()) {
            // With no feedforward, we can compute the output NOW. so let us do it now.
            drainStack();
        }

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
        }
    }
}

