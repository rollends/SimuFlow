package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.codegen.IBlockVisitor;
import ca.rollends.simuflow.blocks.python.*;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.LinkedList;
import java.util.List;

// TODO: Support proper summing block. Right now assuming first is + and rest is -
public class Sum extends BasicBlock {

    private final String signPattern;

    private final SumOperationBuilder builder = new SumOperationBuilder();

    public Sum(int numInputs, String signPattern) {
        super(makeSignalList(numInputs), List.of(new BasicSignal(Dimension.Any, BasicSignal.Type.REAL)));

        assert numInputs >= 1;

        this.signPattern = signPattern;
    }

    private static List<BasicSignal> makeSignalList(int n) {
        List<BasicSignal> list = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            list.add(new BasicSignal(Dimension.Any, BasicSignal.Type.REAL));
        }
        return list;
    }

    private class SumOperationBuilder extends AbstractPythonOperationBuilder {

        public Sequence outputCode() {
            BasicSignal out = getOutputs().get(0);
            Symbol output = out.makeSymbol();
            Symbol input1 = getInputs().get(0).makeSymbol();

            // Declare output with the right size
            Statement makeOutput = new AssignStatement(out.makeSymbol(), Multiply(LiteralReal(0.0), Variable(input1)));
            Sequence outputops = Sequence.from(makeOutput);

            // Add and Subtract components.
            List<BasicSignal> inputs = getInputs();
            for (int i = 0; i < inputs.size(); i++) {
                BasicSignal in = inputs.get(i);

                Statement addStmt = new AssignStatement(out.makeSymbol(), Add(Variable(output), Variable(in.makeSymbol())));
                Statement subStmt = new AssignStatement(out.makeSymbol(), Subtract(Variable(output), Variable(in.makeSymbol())));

                if (signPattern.charAt(i) == '+') {
                    outputops = Sequence.concat(outputops, Sequence.from(addStmt));
                } else if(signPattern.charAt(i) == '-') {
                    outputops = Sequence.concat(outputops, Sequence.from(subStmt));
                }
            }
            return outputops;
        }
    }

    @Override
    public void accept(IBlockVisitor visitor) {
        visitor.visitSum(this);
    }

    @Override
    public Sequence initializationCode() {
        return Sequence.empty();
    }

    @Override
    public Sequence outputCode() {
        return builder.outputCode();
    }
}
