package com.mtanevski.hamcrest.extensions;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

public class ExtendedMatchers extends Matchers {

    /**
     * <p>
     * Creates a matcher that matches when the examined object has values for all of
     * its JavaBean properties that are equal to the corresponding values of the
     * specified bean.
     * </p>
     * For example:
     * <pre>assertThat(myBean, samePropertyValuesAs(myExpectedBean))</pre>
     *
     * @param expectedBean
     *     the bean against which examined beans are compared
     * @param ignoreExtraProperties
     *     ignores extra properties the examined beans could have
     * @param existingPropertiesToIgnore
     *     ignores the existing properties the examined beans could have
     * @param <T> the matching type
     * @return matcher
     */
    public static <T> org.hamcrest.Matcher<T> samePropertyValuesAs(T expectedBean, Boolean ignoreExtraProperties, String... existingPropertiesToIgnore) {
        return SamePropertyValuesAs.<T>samePropertyValuesAs(expectedBean, true, ignoreExtraProperties, existingPropertiesToIgnore);
    }

    /**
     * <p>
     * Creates a matcher that matches when the examined object has values for all of
     * its JavaBean properties that are equal to the corresponding values of the
     * specified bean.
     * </p>
     * For example:
     * <pre>assertThat(myBean, samePropertyValuesAs(myExpectedBean))</pre>
     *
     * @param expectedBean
     *     the bean against which examined beans are compared
     * @param existingPropertiesToIgnore
     *     ignores the existing properties the examined beans could have
     * @param <T> the matching type
     * @return matcher
     */
    public static <T> org.hamcrest.Matcher<T> samePropertyValuesAs(T expectedBean, String... existingPropertiesToIgnore) {
        return SamePropertyValuesAs.<T>samePropertyValuesAs(expectedBean, true, true, existingPropertiesToIgnore);
    }

    /**
     * <p>
     * Creates a matcher that matches when the examined object has values for all of
     * its JavaBean properties that are equal to the corresponding values of the
     * specified bean.
     * </p>
     * For example:
     * <pre>assertThat(myBean, samePropertyValuesAs(myExpectedBean))</pre>
     *
     * @param expectedBean
     *     the bean against which examined beans are compared
     * @param ignoreTypeMatch
     *     ignores the bean types when comparing
     * @param ignoreExtraProperties
     *     ignores extra properties the examined beans could have
     * @param existingPropertiesToIgnore
     *     ignores the existing properties the examined beans could have
     * @param <T> the matching type
     * @return matcher
     */
    public static <T> org.hamcrest.Matcher<T> samePropertyValuesAs(T expectedBean, Boolean ignoreTypeMatch, Boolean ignoreExtraProperties, String... existingPropertiesToIgnore) {
        return SamePropertyValuesAs.<T>samePropertyValuesAs(expectedBean, ignoreTypeMatch, ignoreExtraProperties, existingPropertiesToIgnore);
    }


    /**
     * Checks if a method will fail with exception
     * @param throwableType exception class
     * @param <E> a type of exception
     * @return matcher
     */
    public static <E extends Throwable> Matcher<IThrowingRunnable<E>> failsWith(
            Class<E> throwableType) {
        return new FailWithMatcher<>(instanceOf(throwableType));
    }

    /**
     * Wrapper of existing fail matcher
     * @param throwableType exception class
     * @param <E> a type of exception
     * @param throwableMatcher matcher of existing throwable
     * @return matcher
     */
    public static <E extends Throwable> Matcher<IThrowingRunnable<E>> failsWith(
            Class<E> throwableType,
            Matcher<? super E> throwableMatcher) {
        Matcher<E> matcher = allOf(instanceOf(throwableType), throwableMatcher);
        return new FailWithMatcher<>(matcher);
    }

}
