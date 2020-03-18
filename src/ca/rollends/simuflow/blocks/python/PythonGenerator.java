package ca.rollends.simuflow.blocks.python;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PythonGenerator implements IAbstractSyntaxTreeVisitor {
    private final Deque<Object> codeStack = new LinkedList<>();

    @Override
    public String toString() {
        Object result = codeStack.peek();

        if(result instanceof String) {
            return (String) result;
        }

        List<String> listing = (List<String>) result;

        StringBuilder builder = new StringBuilder();
        listing.forEach( (l) -> builder.append(l) );

        return builder.toString();
    }

    @Override
    public void visitProgram(Program prog) {
        // Generate code for entire program.
        prog.getChildren().stream().forEachOrdered((child) -> child.accept(this));

        // Collapse resulting code into a single piece of code
        List<String> result = (List<String>) codeStack.stream().map( (l) -> {
            // Convert all data types to list!
            if(l instanceof String) {
                return List.of(l);
            } else {
                return l;
            }
        }).reduce( (a, b) -> {
            List<String> al = (List<String>) a;
            List<String> bl = (List<String>) b;
            return Stream.concat(bl.stream(), al.stream()).collect(Collectors.toUnmodifiableList());
        }).get();

        // Clear stack
        codeStack.clear();

        // Push code onto stack.
        codeStack.push(result);
    }

    @Override
    public void visitSequence(Sequence scp) {
        String noTabInFormat = "%s";

        List<String> codeLines = new LinkedList<>();

        List<AbstractSyntaxTree> children = scp.getChildren();
        for(AbstractSyntaxTree child : children) {
            child.accept(this);

            // Inspect top of stack. Two cases.
            Object returnValue = codeStack.poll();
            if(returnValue instanceof String) {

                // Child essentially just a statement.
                codeLines.add(String.format(noTabInFormat, returnValue));

            } else if(returnValue instanceof List) {

                // Child returned a list of code lines. Tab them in
                Stream<String> stream = ((List<String>)returnValue).stream();
                stream = stream.map( (s) -> String.format(noTabInFormat, s) );
                codeLines.addAll(stream.collect(Collectors.toUnmodifiableList()));

            }
        }

        codeStack.push(codeLines);
    }

    @Override
    public void visitStatement(Statement st) {
        Symbol lhs = st.getLHS();
        Expression rhs = st.getRHS();

        // Traverse expression.
        rhs.accept(this);

        // Traverse symbol.
        lhs.accept(this);

        // Top of stack has (1) symbol and (2) rhs code
        StringBuilder strStmt = new StringBuilder();
        strStmt.append((String)codeStack.poll());
        strStmt.append("=");
        strStmt.append((String)codeStack.poll());
        strStmt.append("\n");

        codeStack.push(strStmt.toString());
    }

    @Override
    public void visitScope(Scope scp) {
        String tabInFormat = "    %s";

        List<String> codeLines = new LinkedList<>();

        List<AbstractSyntaxTree> children = scp.getChildren();
        for(AbstractSyntaxTree child : children) {
            child.accept(this);

            // Inspect top of stack. Two cases.
            Object returnValue = codeStack.poll();
            if(returnValue instanceof String) {

                // Child essentially just a statement.
                codeLines.add(String.format(tabInFormat, returnValue));

            } else if(returnValue instanceof List) {

                // Child returned a list of code lines. Tab them in
                Stream<String> stream = ((List<String>)returnValue).stream();
                stream = stream.map( (s) -> String.format(tabInFormat, s) );
                codeLines.addAll(stream.collect(Collectors.toUnmodifiableList()));

            }
        }

        codeStack.push(codeLines);
    }

    @Override
    public void visitFunction(Function fx) {
        List<String> codeLines = new LinkedList<>();

        String defFunctionFormat = "def %s(%s):\n";

        // Make Parameter List
        List<String> parameters = new LinkedList<>();
        for(Symbol parameter : fx.getParameters()) {
            parameter.accept(this);
            parameters.add((String)codeStack.poll());
        }
        if(parameters.size() > 0) {
            codeStack.push(parameters.stream().reduce((a, b) -> a + "," + b).get());
        } else {
            codeStack.push("");
        }

        // Push Function Name
        fx.getName().accept(this);

        String name = (String) codeStack.poll();
        String parameterList = (String) codeStack.poll();

        codeStack.push(String.format(defFunctionFormat, name, parameterList));

        // Visit the scope
        fx.getImplementation().accept(this);

        // Pop the resulting scope implementation and function declaration.
        List<String> impl = (List<String>) codeStack.poll();
        String declaration = (String) codeStack.poll();

        // Push code listing for function
        if(fx.getOutputs().size() > 0) {
            String returnValue = "    return " + fx.getOutputs().get(0).name + "\n"; // TODO: Support statements like return.
            codeStack.push(Stream.concat(Stream.concat(Stream.of(declaration), impl.stream()), Stream.of(returnValue)).collect(Collectors.toUnmodifiableList()));
        } else {
            codeStack.push(Stream.concat(Stream.of(declaration), impl.stream()).collect(Collectors.toUnmodifiableList()));
        }

    }

    @Override
    public void visitExpression(Expression exp) {
        codeStack.push(exp.getExpressionCode());
    }

    @Override
    public void visitSymbol(Symbol sym) {
        codeStack.push(sym.name);
    }
}
