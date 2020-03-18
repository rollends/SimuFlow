package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.Sequence;
import ca.rollends.simuflow.blocks.python.Statement;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockOutputCodeBuilder implements IBlockVisitor {
    private Sequence outputCode;
    private Deque<BasicBlock> operationStack;

    public BlockOutputCodeBuilder() {
        operationStack = new LinkedList<>();
        outputCode = new Sequence(List.of());
    }

    public Sequence getResult() {
        return outputCode;
    }

    @Override
    public void visitSinkBlock(SinkBlock s) {
        // Should be only one input
        BasicSignal signal = s.getInputs().get(0);

        // Push this block on the stack (Last to do anything)
        operationStack.push(s);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        source.accept(this);
    }

    @Override
    public void visitSourceBlock(SourceBlock s) {
        // Push this block on the stack
        operationStack.push(s);

        // Should have the stack full now, so generate output code.
        Sequence operations = new Sequence(List.of());
        while(!operationStack.isEmpty()) {
            BasicBlock block = operationStack.poll();
            operations = new Sequence(Stream.concat(operations.getChildren().stream(), block.makeOutputStep().getChildren().stream()).collect(Collectors.toUnmodifiableList()));
        }

        // Store resulting generated code.
        outputCode = new Sequence(Stream.concat(outputCode.getChildren().stream(), operations.getChildren().stream()).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void visitWire(Wire w) {
        // Should be only one input
        BasicSignal signal = w.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(w);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        source.accept(this);
    }

    @Override
    public void visitStatefulBlock(StatefulBlock s) {
        // TODO: Right now assuming stateful block has only one input. Is this right?
        BasicSignal signal = s.getInputs().get(0);

        // Push this block on the stack
        operationStack.push(s);

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        source.accept(this);
    }
}
