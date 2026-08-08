package org.example.javaOpps.FunctionInterfaceTypes;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
