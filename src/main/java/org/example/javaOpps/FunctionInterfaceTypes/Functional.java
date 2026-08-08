package org.example.javaOpps.FunctionInterfaceTypes;

@FunctionalInterface
public interface Functional <T,R>{
    R apply(T t);
}
