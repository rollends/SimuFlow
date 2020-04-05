package ca.rollends.simuflow.blocks;

import ca.rollends.simuflow.blocks.python.*;
import ca.rollends.simuflow.blocks.traits.Dimension;

import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransferFunction extends StatefulBlock {

    private final List<Double> numerator;
    private final List<Double> denominator;

    private final Symbol A = new Symbol();
    private final Symbol B = new Symbol();
    private final Symbol C = new Symbol();
    private final Symbol D = new Symbol();

    private final TransferFunctionOperationBuilder operationBuilder;

    private final boolean isStrictlyProper;

    public TransferFunction(List<Double> numerator, List<Double> denominator) {
        super(List.of(new BasicSignal(Dimension.Scalar, BasicSignal.Type.REAL)), List.of(new BasicSignal(Dimension.Scalar, BasicSignal.Type.REAL)));

        // Numerator has atleast 0th order terms
        assert numerator.size() >= 1;

        // Denominator has greater order (Transfer Function is proper, real rational => realizable)
        assert numerator.size() <= denominator.size();

        // Denominator has to non-trivial leading coefficient
        assert denominator.get(0) > 0;

        // Create Padding of zeros for numerator
        int padding = (denominator.size()) - numerator.size();
        Stream<Double> zeros = Stream.generate(() -> 0.0).limit(padding);

        if (padding > 0) {
            isStrictlyProper = true;
        } else {
            isStrictlyProper = false;
        }

        // Pad numerator and scale all coefficients by leading denominator coefficient.
        Stream<Double> numeratorStream = Stream.concat(zeros, numerator.stream()).map( (d) -> d / denominator.get(0) );
        Stream<Double> denominatorStream = denominator.stream().map( (d) -> d / denominator.get(0) );

        this.numerator = numeratorStream.collect(Collectors.toUnmodifiableList());
        this.denominator = denominatorStream.collect(Collectors.toUnmodifiableList());
        operationBuilder = new TransferFunctionOperationBuilder();
    }

    public class TransferFunctionOperationBuilder extends AbstractPythonOperationBuilder {
        Symbol numpyZero = new Symbol("np.zeros");
        Symbol numpyMatrix = new Symbol("np.mat");
        Symbol numpyHstack = new Symbol("np.hstack");
        Symbol numpyVstack = new Symbol("np.vstack");
        Symbol numpyEye = new Symbol("np.eye");
        Symbol numpyReshape = new Symbol("np.reshape");

        Symbol pyGetStateForBlock = new Symbol(String.format("getStateForBlock%d", hashCode()));
        Symbol pySetStateForBlock = new Symbol(String.format("setStateForBlock%d", hashCode()));

        public TransferFunctionOperationBuilder() {
            // Useful Variable Names
            Symbol x = new Symbol("x");
            Symbol y = new Symbol("y");
            Symbol index = new Symbol("i");

            // "GetState" operation.
            Sequence extractState = Sequence.empty();
            for (int i = 0; i < getStateSize(); i++) {
                Statement setOneState = new AssignStatement(new Symbol(String.format("y[%d]", i)), new PlainExpression(String.format("x[i+(%d)]", i)));
                extractState = Sequence.concat(extractState, Sequence.from(setOneState));
            }
            Function getStateFor = new Function(pyGetStateForBlock, List.of(x,y,index), List.of(), new Scope(extractState));
            put("getState", getStateFor);


            // "SetState" operation
            Sequence setStates = Sequence.empty();
            for (int i = 0; i < getStateSize(); i++) {
                Statement setOneState = new AssignStatement(new Symbol(String.format("x[i+(%d)]", i)), new PlainExpression(String.format("y[%d]", i)));
                setStates = Sequence.concat(setStates, Sequence.from(setOneState));
            }
            Function setStateFor = new Function(pySetStateForBlock, List.of(x,y,index), List.of(), new Scope(setStates));
            put("setState", setStateFor);
        }

        public void inputOutputConstraint(Map<Symbol, Expression> Matrix, Map<Symbol, Expression> OutputVector) {
            Symbol u = inputs.get(0).makeSymbol();
            Symbol y = outputs.get(0).makeSymbol();
            Symbol x = stateVariable;

            Matrix.put(y, LiteralInteger(1));
            Matrix.put(u, Negative(Variable(D)));
            OutputVector.put(y, Multiply(Variable(C), Variable(x)));
        }

        @Override
        public Sequence outputCode() {
            Symbol y = outputs.get(0).makeSymbol();
            Symbol u = inputs.get(0).makeSymbol();
            Symbol x = stateVariable;

            Statement setY = null;
            if (hasFeedforward()) {
                setY = new AssignStatement(y,
                    Add(
                        Multiply(
                            Variable(C),
                            Variable(x)
                        ),
                        Multiply(
                            Variable(D),
                            Variable(u)
                        )
                    )
                );
            } else {
                setY = new AssignStatement(y,
                    Multiply(
                        Variable(C),
                        Variable(x)
                    )
                );
            }

            return Sequence.from(setY);
        }

        @Override
        public Sequence integrationCode() {
            int N = denominator.size() - 1;

            // Should just be one input and one output by construction (invariant of Class).
            Symbol u = inputs.get(0).makeSymbol();
            Symbol x = stateVariable;
            Symbol dx = dStateVariable;

            Statement setDX = new AssignStatement(dx,
                Add(
                    Multiply(
                        Variable(A),
                        Variable(x)
                    ),
                    Multiply(
                        Variable(B),
                        Variable(u)
                    )
                )
            );

            return Sequence.from(setDX);
        }

        @Override
        public Sequence stepSetupCode() {
            // Add Operation to Read state for this block from entire state vector.
            Statement declareStateVar = new AssignStatement(getStateVariable(), Call(numpyZero, List.of(Tuple(LiteralInteger(getStateSize()), LiteralInteger(1)))));

            // Get function implementations
            return Sequence.from(declareStateVar);
        }

        @Override
        public Sequence preparationCode() {
            int N = denominator.size() - 1;

            // Create Initial Condition
            Statement assignX0 = new AssignStatement(initialStateVariable, Call(numpyZero, List.of(LiteralInteger(N))));

            // Create Output Matrix C
            Stream<String> numCoeffs = numerator.stream().skip(1).map((d) -> d.toString());
            String numCoeffsArray = numCoeffs.reduce((a, b) -> b + " " + a).get();
            Statement assignC = new AssignStatement(C, Call(numpyMatrix, List.of(LiteralString(numCoeffsArray))));

            // Create Feedforward Matrix D
            Statement assignD = new AssignStatement(D,
                Call(
                    numpyMatrix,
                    List.of(
                        LiteralString(numerator.get(0).toString())
                    )
                )
            );

            // Create State Transition Matrix A
            Stream<String> denCoeffs = denominator.stream().skip(1).map((d) -> -d).map((d) -> d.toString());
            String denCoeffsArray = denCoeffs.reduce((s, d) -> d + " " + s).get();
            Symbol t1 = new Symbol();
            Statement assignT1 = new AssignStatement(t1,
                Call(
                    numpyHstack,
                    List.of(
                        Tuple(
                            Call(numpyZero, List.of(Tuple(LiteralInteger(N - 1),LiteralInteger(1)))),
                            Call(numpyEye, List.of(LiteralInteger(N - 1)))
                        )
                    )
                )
            );
            Statement assignA = new AssignStatement(A,
                Call(
                    numpyVstack,
                    List.of(
                        Tuple(
                            Variable(t1),
                            Call(
                                numpyMatrix,
                                List.of(LiteralString(denCoeffsArray))
                            )
                        )
                    )
                )
            );

            // Create Control Matrix B
            Statement assignB = new AssignStatement(B,
                Call(
                    numpyVstack,
                    List.of(
                        Tuple(
                            Call(
                                numpyZero, List.of(Tuple(LiteralInteger(N-1), LiteralInteger(1)))
                            ),
                            LiteralReal(1.0)
                        )
                    )
                )
            );

            return Sequence.from(getImplementation(), assignX0, assignD, assignC, assignT1, assignA, assignB);
        }
    }

    @Override
    public AbstractPythonOperationBuilder getBuilder() {
        return operationBuilder;
    }

    @Override
    public Sequence initializationCode() {
        return operationBuilder.preparationCode();
    }

    @Override
    public Sequence integrationCode() {
        return operationBuilder.integrationCode();
    }

    @Override
    public Sequence outputCode() {
        return operationBuilder.outputCode();
    }

    @Override
    public boolean hasFeedforward() {
        return !isStrictlyProper;
    }

    @Override
    public int getStateSize() {
        return denominator.size() - 1;
    }
}
