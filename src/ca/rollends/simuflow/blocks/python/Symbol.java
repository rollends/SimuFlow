package ca.rollends.simuflow.blocks.python;

import ca.rollends.simuflow.blocks.traits.IEquivalenceGroupoid;

public class Symbol extends AbstractSyntaxTree implements IEquivalenceGroupoid<Symbol> {
    public final String name;

    public Symbol() { this.name = String.format("t_%d", hashCode()); }
    public Symbol(String name) {
        this.name = name;
    }

    @Override
    public boolean isEquivalent(Symbol other) {
        return other.name.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(IAbstractSyntaxTreeVisitor visitor) {
        visitor.visitSymbol(this);
    }
}
