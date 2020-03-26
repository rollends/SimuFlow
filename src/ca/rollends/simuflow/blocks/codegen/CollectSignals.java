package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.*;

import java.util.*;

public class CollectSignals implements IBlockVisitor {

    private Deque<BasicBlock> probeStack = new LinkedList<>();
    private Set<BasicBlock> closedSet = new HashSet<>();
    private Set<BasicBlock> visitedSet = new HashSet<>();
    private Sequence resultCode = Sequence.empty();
    private Sequence activeCode = Sequence.empty();
    private CollectBlockStates stateCollector = null;
    private boolean activeOnProbe = false;
    private Symbol signalMap = new Symbol("signalMap");

    public CollectSignals(CollectBlockStates stateCollector) {
        this.stateCollector = stateCollector;

        resultCode = Sequence.from(new AssignStatement(signalMap, new PlainExpression("dict()")));
    }

    public Sequence getResultCode() {
        return resultCode;
    }

    @Override
    public void visitSinkBlock(SinkBlock s) {
        BasicSignal signal = s.getInputs().get(0);
        BasicBlock source = signal.getSource();

        source.accept(this);
    }

    private void drainStack() {
        while(!probeStack.isEmpty()) {
            BasicBlock block = probeStack.poll();
            activeCode = Sequence.concat(activeCode, block.outputCode());
        }
    }


    @Override
    public void visitSourceBlock(SourceBlock s) {
        // Done.
        if (activeOnProbe) {
            probeStack.push(s);
        } else if(!visitedSet.contains(s)) {
            visitedSet.add(s);
        } else {
            return;
        }
    }

    @Override
    public void visitWire(Wire w) {
        if (activeOnProbe) {
            probeStack.push(w);
        } else if(!visitedSet.contains(w)) {
            visitedSet.add(w);
        } else {
            return;
        }

        // Recurse down until we find an output!
        BasicSignal signal = w.getInputs().get(0);
        BasicBlock source = signal.getSource();

        source.accept(this);
    }

    @Override
    public void visitProbe(Probe w) {
        BasicSignal signal = w.getInputs().get(0);
        BasicBlock source = signal.getSource();

        if (!activeOnProbe) {
            if(closedSet.contains(w)) {
                return; // already did this probe.
            }

            Sequence result = null;

            // We make this the active probe, for which we will generate the output function.
            {
                activeOnProbe = true;
                activeCode = Sequence.empty();
                closedSet.add(w);
                probeStack.push(w);

                // Cache Visited Set
                Set<BasicBlock> cachedVisited = visitedSet;
                visitedSet = new HashSet<>();

                // Recurse down until we find an output!
                source.accept(this);

                visitedSet.addAll(cachedVisited);

                // Done this probe (drain the stack).
                drainStack();
                result = activeCode;

                // Reset state
                activeOnProbe = false;
                activeCode = Sequence.empty();
            }

            NumpyOperations numpy = new NumpyOperations();

            Symbol x_sol = new Symbol("x_sol");
            Symbol x = new Symbol("x");
            Symbol t_sol = new Symbol("t_sol");
            Symbol time = new Symbol("time");
            Symbol y = new Symbol("y");
            Symbol probeOut = signal.makeSymbol();

            Function fx = new Function(
                    new Symbol(String.format("getSignal%s", w.getName())),
                    List.of(t_sol, x_sol),
                    List.of(y),
                    new Scope(
                        Sequence.from(
                            new AssignStatement(time, new PlainExpression("t_sol[0]")),
                            new AssignStatement(x, new PlainExpression(String.format("%s[:, 0]", x_sol.toString()))),
                            result,
                            new AssignStatement(y, numpy.zeros(
                               new TupleExpression(List.of(
                                   numpy.size(new PlainExpression(probeOut.toString())),
                                   numpy.size(new PlainExpression(x_sol.toString()), new PlainExpression("1"))
                               ))
                            )),
                            new PlainStatement("for ti in range(0, np.size(y, 1)):"),
                            new Scope(Sequence.from(
                                new AssignStatement(time, new PlainExpression("t_sol[ti]")),
                                new AssignStatement(x, new PlainExpression(String.format("%s[:, ti]", x_sol.toString(), 0))),
                                result,
                                new AssignStatement(new Symbol("y[:, ti]"), new PlainExpression(probeOut.toString()))
                            )),
                            new ReturnStatement(new PlainExpression(y.toString()))
                        )
                    )
                );

            resultCode = Sequence.concat(resultCode, Sequence.from(fx));
        } else {
            probeStack.push(w);
        }

        // Recurse down to find output either for another probe or to find a new probe.
        source.accept(this);
    }

    @Override
    public void visitNode(Node w) {
        // If we are inspecting outputs, keep inspecting!
        if (activeOnProbe) {
            probeStack.push(w);
        } else if(!visitedSet.contains(w)) {
            visitedSet.add(w);
        } else {
            return;
        }

        // Recurse down until we find an output!
        BasicSignal signal = w.getInputs().get(0);
        BasicBlock source = signal.getSource();

        source.accept(this);
    }

    @Override
    public void visitSum(Sum s) {
        Deque<BasicBlock> cachedStack = new LinkedList<>();

        if(activeOnProbe) {
            cachedStack.addAll(probeStack);
            probeStack.clear();
        } else if(!visitedSet.contains(s)) {
            visitedSet.add(s);
        } else {
            return;
        }

        // More than one input
        for(BasicSignal signal : s.getInputs()) {
            // Figure out source block
            BasicBlock source = signal.getSource();

            // Traverse
            source.accept(this);

            if (activeOnProbe) {
                drainStack();
            }
        }

        if(activeOnProbe) {
            probeStack.addAll(cachedStack);
            probeStack.push(s);
        }
    }

    @Override
    public void visitStatefulBlock(StatefulBlock s) {
        // If we are inspecting outputs, keep inspecting!
        if (activeOnProbe) {
            probeStack.push(s);

            // Since we need the output of a stateful block, we need its states in our local implementation.
            activeCode = Sequence.concat(activeCode, s.getBuilder().stepSetupCode());
            Statement writeToState = new FunctionCallStatement(s.getBuilder().get("getState"), List.of(new PlainExpression("x"), new PlainExpression(s.getStateVariable().toString()), new PlainExpression(stateCollector.getStateIndexMap().get(s.getStateVariable()).toString())));
            activeCode = Sequence.concat(activeCode, Sequence.from(writeToState));

            if(!s.hasFeedforward()) {
                // With no feedforward, we can compute the output NOW. so let us do it now.
                return;
            }
        } else if(!visitedSet.contains(s)) {
            visitedSet.add(s);
        } else {
            return;
        }

        // Recurse down until we find an output!
        BasicSignal signal = s.getInputs().get(0);
        BasicBlock source = signal.getSource();

        source.accept(this);
    }
}
