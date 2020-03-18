package ca.rollends.simuflow.blocks.python;

import java.util.List;

public class Function extends AbstractSyntaxTree {

    private final Symbol name;
    private final List<Symbol> parameters;
    private final List<Symbol> outputs;
    private final Scope scope;

    public Function(Symbol name, List<Symbol> parameters, List<Symbol> outputs, Scope impl) {
        this.name = name;
        this.parameters = parameters;
        this.outputs = outputs;
        this.scope = impl;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitFunction(this);
    }

    public Symbol getName() { return name; }
    public List<Symbol> getParameters() { return parameters; }
    public Scope getImplementation() { return scope; }
}
