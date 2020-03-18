package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.python.*;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransferFunction extends StatefulBlock {

    private final List<Double> numerator;
    private final List<Double> denominator;

    private final Symbol A = new Symbol();
    private final Symbol B = new Symbol();
    private final Symbol C = new Symbol();

    public TransferFunction(List<Double> numerator, List<Double> denominator) {
        super(List.of(new BasicSignal(Dimension.Scalar, BasicSignal.Type.REAL)), List.of(new BasicSignal(Dimension.Scalar, BasicSignal.Type.REAL)));

        // Numerator has atleast 0th order terms
        assert numerator.size() >= 1;

        // Denominator has greater order (Transfer Function is strictly proper => realizable)
        // TODO: Support Proper Transfer Functions
        assert numerator.size() < denominator.size();

        // Denominator has to non-trivial leading coefficient
        assert denominator.get(0) > 0;

        // Create Padding of zeros for numerator
        int padding = (denominator.size()-1) - numerator.size();
        Stream<Double> zeros = Stream.generate(() -> 0.0).limit(padding);

        // Pad numerator and scale all coefficients by leading denominator coefficient.
        Stream<Double> numeratorStream = Stream.concat(zeros, numerator.stream()).map( (d) -> d / denominator.get(0) );
        Stream<Double> denominatorStream = denominator.stream().map( (d) -> d / denominator.get(0) );

        this.numerator = numeratorStream.collect(Collectors.toUnmodifiableList());
        this.denominator = denominatorStream.collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Sequence makePreparationStep() {
        int N = denominator.size() - 1;

        // Create Initial Condition
        Statement assignX0 = new AssignStatement(initialStateVariable, new Expression(String.format("np.zeros(%d)", N)));

        // Create Output Matrix C
        Stream<String> numCoeffs = numerator.stream().map((d) -> d.toString());
        Statement assignC = new AssignStatement(C, new Expression(String.format("np.mat('%s')", numCoeffs.reduce((a, b) -> b + " " + a).get())));

        // Create State Transition Matrix A
        Stream<String> denCoeffs = denominator.stream().skip(1).map((d) -> -d).map((d) -> d.toString());
        Symbol t1 = new Symbol();
        Statement assignT1 = new AssignStatement(t1, new Expression(String.format("np.hstack((np.zeros((%1$d,1)), np.eye(%1$d)))", N - 1)));
        Statement assignA = new AssignStatement(A, new Expression(String.format("np.vstack((%1$s, np.mat('%2$s')))", t1, denCoeffs.reduce((s, d) -> d + " " + s).get())));

        // Create Control Matrix B
        Statement assignB = new AssignStatement(B, new Expression(String.format("np.vstack((np.zeros((%d,1)),1.0))", N - 1)));

        return new Sequence(List.of(assignX0, assignC, assignT1, assignA, assignB));
    }

    @Override
    public Sequence makeIntegrationStep() {
        int N = denominator.size() - 1;

        // Should just be one input and one output by construction (invariant of Class).
        Symbol u = inputs.get(0).makeSymbol();
        Symbol x = stateVariable;
        Symbol dx = dStateVariable;

        Statement setDX = new AssignStatement(dx, new Expression(String.format("%s * ( %s.reshape(%5$d, 1) ) + %s * ( %s.reshape(1, 1) )", A, x, B, u, N)));

        return new Sequence(List.of(setDX));
    }

    @Override
    public Sequence makeOutputStep() {
        int N = denominator.size() - 1;

        Symbol y = outputs.get(0).makeSymbol();
        Symbol x = stateVariable;

        Statement setY = new AssignStatement(y, new Expression(String.format("np.squeeze(np.asarray(%s * ( %s.reshape(%3$d, 1) )), axis=0)", C, x, N)));

        return new Sequence(List.of(setY));
    }
}
