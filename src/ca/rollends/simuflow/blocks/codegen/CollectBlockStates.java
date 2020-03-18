package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.Symbol;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CollectBlockStates implements IBlockVisitor {
    private Set<BasicBlock> visitedSet;
    private List<Symbol> initialStates;
    private List<Symbol> states;
    private List<Symbol> differentials;

    public CollectBlockStates() {
        this.visitedSet = new HashSet<>();
        this.states = new LinkedList<>();
        this.differentials = new LinkedList<>();
        this.initialStates = new LinkedList<>();
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
    public void visitStatefulBlock(StatefulBlock s) {
        visitedSet.add(s);

        // Follow the signal backwards
        BasicSignal signal = s.getInputs().get(0);

        // Add states.
        initialStates.add(s.getInitialStateVariable());
        states.add(s.getStateVariable());
        differentials.add(s.getDifferentialVariable());

        // Figure out source block
        BasicBlock source = signal.getSource();

        // Traverse
        if(!visitedSet.contains(source)) {
            source.accept(this);
        }
    }
}
