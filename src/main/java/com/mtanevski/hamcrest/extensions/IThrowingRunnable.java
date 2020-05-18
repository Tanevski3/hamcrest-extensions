package com.mtanevski.hamcrest.extensions;

@FunctionalInterface
public interface IThrowingRunnable<E extends Throwable> {
    void run() throws E;
}
