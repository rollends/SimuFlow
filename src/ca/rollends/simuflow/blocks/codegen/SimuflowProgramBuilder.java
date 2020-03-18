package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.*;

import java.util.LinkedList;
import java.util.List;

public class SimuflowProgramBuilder  {

    private final BlockDiagram diagram;
    private final Sequence imports;

    public SimuflowProgramBuilder(BlockDiagram diagram) {
        this.diagram = diagram;

        imports = new Sequence(List.of(
                    new ImportStatement(new Expression("numpy as np")),
                    new ImportStatement(new Expression("scipy as sp")),
                    new ImportStatement(new Expression("scipy.integrate"))
        ));
    }

    public Program build() {
        Program prog = Program.newProgram();
        prog = Program.newProgram(prog, imports);

        // Preparation Logic is just statements that are run before doing anything else
        for(BasicBlock b : diagram.getBlocks()) {
            prog = Program.newProgram(prog, b.makePreparationStep());
        }

        // Now we create the much needed simulation step function.
        prog = Program.newProgram(prog, makeGlobalStepFunction());

        // Now insert the solver code
        prog = Program.newProgram(prog, makeSolverCode());

        return prog;
    }

    private Sequence makeSolverCode() {
        Symbol solution = new Symbol("x");
        Symbol initialCondition = new Symbol("x0");

        // Collect all the states and their differentials in-order.
        CollectBlockStates collectBlockStates = new CollectBlockStates();
        diagram.accept(collectBlockStates);

        // Set initial condition
        List<Symbol> initialStates = collectBlockStates.getInitialStates();
        String cols = initialStates.stream().map((l) -> l.toString()).reduce((a,b) -> a + "," + b).get();
        Sequence assignInitialCondition = Sequence.from(new AssignStatement(initialCondition, new Expression(String.format("np.squeeze(np.asarray(np.vstack( (%s) )), axis=0)", cols))));

        // Run solver to time Tmax = 10
        double Tmax = 10.;
        Statement solve = new AssignStatement(solution, new Expression(String.format("sp.integrate.solve_ivp(flow, [0, %g], %s)", Tmax, initialCondition)));

        return Sequence.concat(assignInitialCondition, Sequence.from(solve));
    }

    private Function makeGlobalStepFunction() {
        Symbol x = new Symbol("x");
        Symbol dx = new Symbol("dx");

        // Collect all the states and their differentials in-order.
        CollectBlockStates collectBlockStates = new CollectBlockStates();
        diagram.accept(collectBlockStates);

        // Create assignment of state variables
        List<Symbol> states = collectBlockStates.getStates();
        List<AbstractSyntaxTree> assignStateVars = new LinkedList<>();
        for(int i = 0; i < states.size(); i++) {
            Expression index = new Expression(String.format("%s[%d]", x, i));
            assignStateVars.add(new AssignStatement(states.get(i), index));
        }
        Sequence assignStates = new Sequence(assignStateVars);

        // First need to compute the output logic.
        BlockOutputCodeBuilder outputCodeBuilder = new BlockOutputCodeBuilder();
        diagram.accept(outputCodeBuilder);
        Sequence blockOutputCode = outputCodeBuilder.getResult();

        // Collect integration steps.
        IntegrationStepCodeBuilder integrationStepCodeBuilder = new IntegrationStepCodeBuilder();
        diagram.accept(integrationStepCodeBuilder);
        Sequence integration = integrationStepCodeBuilder.getResult();

        // And output assignment of differentials
        List<Symbol> differentials = collectBlockStates.getDifferentials();
        List<AbstractSyntaxTree> assignDifferentials = new LinkedList<>();

        String cols = differentials.stream().map((l) -> "[" + l.toString() + "]").reduce((a,b) -> a + "," + b).get();
        assignDifferentials.add(new AssignStatement(dx, new Expression(String.format("np.squeeze(np.asarray(np.bmat([%s])))", cols))));
        Sequence assignDX = new Sequence(assignDifferentials);

        // Final Sequence of Code
        Sequence implementation = Sequence.concat(assignStates, blockOutputCode, integration, assignDX);

        return new Function(new Symbol("flow"), List.of(BasicBlock.time.makeSymbol(), x), List.of(dx), new Scope(implementation));
    }

}
