package ca.rollends.simuflow.blocks.codegen;

import ca.rollends.simuflow.blocks.*;
import ca.rollends.simuflow.blocks.python.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimuflowProgramBuilder  {

    private final BlockDiagram diagram;

    public SimuflowProgramBuilder(BlockDiagram diagram) {
        this.diagram = diagram;
    }

    public Program build() {
        Program prog = Program.newProgram();

        // Preparation Logic is just statements that are run before doing anything else
        for(BasicBlock b : diagram.getBlocks()) {
            prog = Program.newProgram(prog, b.makePreparationStep());
        }

        // Now we create the much needed simulation step function.
        Function step = makeGlobalStepFunction();
        prog = Program.newProgram(prog, step);

        return prog;
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
        assignDifferentials.add(new AssignStatement(dx, new Expression(String.format("bmat([%s])", cols))));
        Sequence assignDX = new Sequence(assignDifferentials);

        // Final Sequence of Code
        Sequence implementation = Sequence.concat(Sequence.concat(assignStates, Sequence.concat(blockOutputCode, integration)), assignDX);

        return new Function(new Symbol("flow"), List.of(BasicBlock.time.makeSymbol(), x), List.of(dx), new Scope(implementation));
    }

}
