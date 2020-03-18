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
    public Sequence makePreparationStep() {
        return new Sequence(List.of());
    }

    @Override
    public Sequence makeOutputStep() {
        // Output Variable
        Symbol y = outputs.get(0).makeSymbol();
        Symbol t = time.makeSymbol();

        // Generate output value.
        Statement setOutput = new AssignStatement(y, new Expression(String.format("%g if %s > %g else 0", amplitude.doubleValue(), t, startTime)));

        return new Sequence(List.of(setOutput));
    }
}
