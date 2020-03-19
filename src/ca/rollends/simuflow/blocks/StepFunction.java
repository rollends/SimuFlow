package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.python.*;
import ca.rollends.simuflow.blocks.traits.Dimension;

import java.util.List;

public class StepFunction extends SourceBlock {

    private final Double amplitude;
    private final Double startTime;

    public StepFunction(Double amplitude, Double startTime) {
        super(List.of(new BasicSignal(Dimension.Scalar, BasicSignal.Type.REAL)));
        this.amplitude = amplitude;
        this.startTime = startTime;
    }

    @Override
    public Sequence initializationCode() {
        return new Sequence(List.of());
    }

    @Override
    public Sequence outputCode() {
        // Output Variable
        Symbol y = outputs.get(0).makeSymbol();
        Symbol t = time.makeSymbol();

        // Generate output value.
        Statement setOutput = new AssignStatement(y, new PlainExpression(String.format("(%g*np.ones(1) if %s >= %g else np.zeros(1))", amplitude.doubleValue(), t, startTime)));

        return new Sequence(List.of(setOutput));
    }
}
