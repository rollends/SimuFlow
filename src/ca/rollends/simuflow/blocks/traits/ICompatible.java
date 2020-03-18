package ca.rollends.simuflow.blocks.traits;

public interface ICompatible<T> {
    default boolean isCompatibleWith(T k) {
        return false;
    }
}
