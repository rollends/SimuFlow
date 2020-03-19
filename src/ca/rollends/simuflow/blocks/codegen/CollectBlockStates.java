package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.*;

import javax.swing.plaf.nimbus.State;
import java.util.*;

public class CollectBlockStates implements IBlockVisitor {
    private Set<BasicBlock> visitedSet;
    private List<Symbol> initialStates;
    private List<Symbol> states;
    private List<Symbol> differentials;
    private List<Integer> sizes;
    private Sequence stateOperations;
    private Sequence stateReadingOperations;
    private Sequence differentialWritingOperations;
    private int currentIndex;

    public CollectBlockStates() {
        this.visitedSet = new HashSet<>();
        this.states = new LinkedList<>();
        this.differentials = new LinkedList<>();
        this.initialStates = new LinkedList<>();
        this.sizes = new ArrayList<>();
        currentIndex = 0;
        stateOperations = Sequence.empty();
        stateReadingOperations = Sequence.empty();
        differentialWritingOperations = Sequence.empty();
    }

    public Sequence getStateOperations() {
        return stateOperations;
    }

    public Sequence getDifferentialWritingOperations() {
        return differentialWritingOperations;
    }

    public Sequence getStateReadingOperations() {
        return stateReadingOperations;
    }

    public List<Symbol> getDifferentials() {
        return differentials;
    }

    public List<Symbol> getStates() {
        return states;
    }

    public List<Symbol> getInitialStates() {
        return initialStates;
    }

    public List<Integer> getStateSizes() {
        return sizes;
    }

    @Override
    public void visitSinkBlock(SinkBlock s) {
        // Follow the signal backwards
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

        // Follow the signal backwards
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

        // Follow the signal backwards
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

        // Follow the signal backwards
        BasicSignal signal = s.getInputs().get(0);

        // Add states.
        initialStates.add(s.getInitialStateVariable());
        states.add(s.getStateVariable());
        differentials.add(s.getDifferentialVariable());
        sizes.add(s.getStateSize());

        stateReadingOperations = Sequence.concat(stateReadingOperations, s.getBuilder().stepSetupCode(currentIndex));
        currentIndex += s.getStateSize();

        // Add Operation to Write differential state for this block into the differential output.
        Statement writeToDifferential = new PlainStatement(String.format("%s(dx,%s)", s.getPySetStateForBlock(), s.getDifferentialVariable()));
        differentialWritingOperations = Sequence.concat(differentialWritingOperations, Sequence.from(writeToDifferential));

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }
}
