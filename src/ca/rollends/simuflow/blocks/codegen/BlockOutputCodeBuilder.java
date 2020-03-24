package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.codegen.exceptions.AlgebraicLoopDetectedException;
import ca.rollends.simuflow.blocks.codegen.exceptions.SimuflowCompilationExceptionList;
import ca.rollends.simuflow.blocks.python.Sequence;

import java.util.*;

public class BlockOutputCodeBuilder implements IBlockVisitor {
    private Sequence outputCode;
    private Set<BasicBlock> closedSet = new HashSet<>();
    private Set<BasicBlock> openSet = new HashSet<>();
    private Deque<BasicBlock> operationStack;
    private SimuflowCompilationExceptionList errors = SimuflowCompilationExceptionList.empty();

    public BlockOutputCodeBuilder() {
        operationStack = new LinkedList<>();
        outputCode = new Sequence(List.of());
    }

    public Sequence getResult() throws SimuflowCompilationExceptionList {
        if (!errors.isEmpty()) {
            throw errors;
        }
        return outputCode;
    }

    @Override
    public void visitSinkBlock(SinkBlock s) {
        openSet.add(s);

        // Should be only one input
        BasicSignal signal = s.getInputs().get(0);

        // Push this block on the stack (Last to do anything)
        operationStack.push(s);

        // Figure out source block
        BasicBlock source = signal.getSource();

        if (openSet.contains(source)) {
            errors = errors.concat(SimuflowCompilationExceptionList.from(new AlgebraicLoopDetectedException(source)));
            drainStack();
            return;
        }

        // Traverse
        if(!closedSet.contains(source)) {
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

            openSet.remove(block);
            closedSet.add(block);
        }
    }

    @Override
    public void visitSourceBlock(SourceBlock s) {
        openSet.add(s);

        // Push this block on the stack
        operationStack.push(s);

        drainStack();
    }

    @Override
    public void visitWire(Wire w) {
        openSet.add(w);

        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(w);

        // Figure out source block
        BasicBlock source = signal.getSource();

        if (openSet.contains(source)) {
            errors = errors.concat(SimuflowCompilationExceptionList.from(new AlgebraicLoopDetectedException(source)));
            drainStack();
            return;
        }

        // Traverse
        if(!closedSet.contains(source)) {
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
        openSet.add(w);

        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(w);

        // Figure out source block
        BasicBlock source = signal.getSource();

        if (openSet.contains(source)) {
            errors = errors.concat(SimuflowCompilationExceptionList.from(new AlgebraicLoopDetectedException(source)));
            drainStack();
            return;
        }

        // Traverse
        if(!closedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
        }
    }

    @Override
    public void visitSum(Sum s) {
        openSet.add(s);

        // Clean stack
        Deque<BasicBlock> oldStack = new LinkedList<>();
        oldStack.addAll(operationStack);
        operationStack.clear();

        // More than one input
        for(BasicSignal signal : s.getInputs()) {
            // Figure out source block
            BasicBlock source = signal.getSource();

            if (openSet.contains(source)) {
                errors = errors.concat(SimuflowCompilationExceptionList.from(new AlgebraicLoopDetectedException(source)));
                drainStack();
                return;
            }

            // Traverse
            if (!closedSet.contains(source)) {
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
        openSet.add(s);

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

        if (openSet.contains(source)) {
            errors = errors.concat(SimuflowCompilationExceptionList.from(new AlgebraicLoopDetectedException(source)));
            drainStack();
            return;
        }

        // Traverse
        if(!closedSet.contains(source)) {
            source.accept(this);
        } else {
            drainStack();
        }
    }
}

