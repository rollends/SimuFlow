package ca.rollends.simuflow.blocks.python;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractPythonOperationBuilder {

    private final HashMap<String, Symbol> operationTable = new HashMap<>();
    private final List<Function> implementation = new LinkedList<>();

    public Sequence getImplementation() {
        Sequence total = Sequence.empty();
        for (Function fx : implementation) {
            total = Sequence.concat(total, Sequence.from(fx));
        }
        return total;
    }

    public Symbol get(String name) {
        return operationTable.get(name);
    }

    protected void put(String name, Function operation) {
        operationTable.put(name, operation.getName());
        implementation.add(operation);
    }

    // Helper Methods to prevent torture in forming expressions.
    protected Expression Negative(Expression operand) {
        return new UnaryOperatorExpression("-", operand);
    }

    protected Expression Add(Expression lhs, Expression rhs) {
        return new BinaryOperatorExpression(lhs, "+", rhs);
    }

    protected Expression Subtract(Expression lhs, Expression rhs) {
        return new BinaryOperatorExpression(lhs, "-", rhs);
    }

    protected Expression Multiply(Expression lhs, Expression rhs) {
        return new BinaryOperatorExpression(lhs, "*", rhs);
    }

    protected Expression Exponentiate(Expression lhs, Expression rhs) {
        return new BinaryOperatorExpression(lhs, "**", rhs);
    }

    protected Expression Divide(Expression lhs, Expression rhs) {
        return new BinaryOperatorExpression(lhs, "/", rhs);
    }

    protected Expression Variable(Symbol b) {
        return new PlainExpression(b.toString());
    }

    protected Expression Call(Symbol functionName, List<Expression> arguments) {
        return new FunctionCallExpression(functionName, arguments);
    }

    protected Expression Tuple(Expression ex1, Expression ex2, Expression ... exOther) {
        List<Expression> exprs = new LinkedList<>();

        exprs.add(ex1);
        exprs.add(ex2);
        exprs.addAll(Arrays.stream(exOther).collect(Collectors.toUnmodifiableList()));

        return new TupleExpression(exprs);
    }

    protected Expression LiteralInteger(Integer i) {
        return new PlainExpression(i.toString());
    }

    protected Expression LiteralReal(Double d) {
        return new PlainExpression(d.toString());
    }

    protected Expression LiteralString(String s) {
        if (!s.contains("'")) {
            return new PlainExpression(String.format("'%s'", s));
        } else if (!s.contains("\"")) {
            return new PlainExpression(String.format("\"%s\"", s));
        } else {
            return new PlainExpression(String.format("'%s'", s.replace("'", "\\'")));
        }
    }

    public Sequence stepSetupCode(Integer stateIndex) {
        return Sequence.empty();
    }

    public Sequence outputCode() {
        return Sequence.empty();
    }

    public Sequence integrationCode() {
        return Sequence.empty();
    }

    public Sequence preparationCode() {
        return Sequence.empty();
    }
}
