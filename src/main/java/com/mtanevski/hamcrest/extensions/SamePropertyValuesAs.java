package com.mtanevski.hamcrest.extensions;

import org.hamcrest.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.beans.PropertyUtil.NO_ARGUMENTS;
import static org.hamcrest.beans.PropertyUtil.propertyDescriptorsFor;
import static org.hamcrest.core.IsEqual.equalTo;

public class SamePropertyValuesAs<T> extends TypeSafeDiagnosingMatcher<T> {

    private final T expectedBean;
    private final Set<String> propertyNames;
    private final Boolean ignoreTypeMatch;
    private final Boolean ignoreExtraProperties;
    private final List<PropertyMatcher> propertyMatchers;
    private final List<String> existingPropertiesToIgnore;


    public SamePropertyValuesAs(T expectedBean, Boolean ignoreTypeMatch, Boolean ignoreExtraProperties, String... existingPropertiesToIgnore) {
        PropertyDescriptor[] descriptors = propertyDescriptorsFor(expectedBean, Object.class);
        this.ignoreTypeMatch = ignoreTypeMatch;
        this.ignoreExtraProperties = ignoreExtraProperties;
        this.expectedBean = expectedBean;
        this.propertyNames = propertyNamesFrom(descriptors);
        this.propertyMatchers = propertyMatchersFor(expectedBean, descriptors);
        this.existingPropertiesToIgnore = getExistingPropertiesToIgnore(existingPropertiesToIgnore);
    }

    private static List<String> getExistingPropertiesToIgnore(String[] existingPropertiesToIgnore) {
        List<String> properties = new ArrayList<>();
        if (existingPropertiesToIgnore != null) {
            properties.addAll(Arrays.asList(existingPropertiesToIgnore));
        }
        return properties;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("same property values as " + expectedBean.getClass().getSimpleName())
                .appendList(" [", ", ", "]", propertyMatchers);
    }


    private boolean isCompatibleType(T item, Description mismatchDescription) {
        if (!expectedBean.getClass().isAssignableFrom(item.getClass())) {
            mismatchDescription.appendText("is incompatible type: " + item.getClass().getSimpleName());
            return false;
        }
        return true;
    }

    private boolean hasNoExtraProperties(T item, Description mismatchDescription) {
        Set<String> actualPropertyNames = propertyNamesFrom(propertyDescriptorsFor(item, Object.class));
        actualPropertyNames.removeAll(propertyNames);
        if (!actualPropertyNames.isEmpty()) {
            mismatchDescription.appendText("has extra properties called " + actualPropertyNames);
            return false;
        }
        return true;
    }

    private static Object readProperty(Method method, Object target) throws NoSuchMethodException {
        try {
            Method actualMethod = target.getClass().getDeclaredMethod(method.getName());
            actualMethod.setAccessible(true);
            Object value = actualMethod.invoke(target, NO_ARGUMENTS);
            if (value != null && value instanceof Enum<?>) {
                value = value.toString();
            }
            return value;
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not invoke " + method + " on " + target, e);
        }
    }

    private static <T> List<PropertyMatcher> propertyMatchersFor(T bean, PropertyDescriptor[] descriptors) {
        List<SamePropertyValuesAs.PropertyMatcher> result = new ArrayList<>(descriptors.length);
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            result.add(new PropertyMatcher(propertyDescriptor, bean));
        }
        return result;
    }

    private static Set<String> propertyNamesFrom(PropertyDescriptor[] descriptors) {
        HashSet<String> result = new HashSet<>();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            result.add(propertyDescriptor.getDisplayName());
        }
        return result;
    }

    /**
     * <p>
     * Creates a matcher that matches when the examined object has values for all of
     * its JavaBean properties that are equal to the corresponding values of the
     * specified bean.
     * </p>
     * For example:
     * <pre>assertThat(actualBean, samePropertyValuesAs(expectedBean))</pre>
     *
     * @param expectedBean               the bean against which examined beans are compared
     * @param ignoreTypeMatch            ignores the bean types when comparing
     * @param ignoreExtraProperties      ignores when comparing extra properties
     * @param existingPropertiesToIgnore ignores comparing existing properties
     * @param <T>                        type
     * @return matcher
     */
    @Factory
    public static <T> Matcher<T> samePropertyValuesAs(T expectedBean, Boolean ignoreTypeMatch, Boolean ignoreExtraProperties, String... existingPropertiesToIgnore) {
        return new SamePropertyValuesAs<>(expectedBean, ignoreTypeMatch, ignoreExtraProperties, existingPropertiesToIgnore);
    }

    @Override
    public boolean matchesSafely(T bean, Description mismatch) {

        boolean matchingResult = true;

        if (!ignoreTypeMatch)
            matchingResult = isCompatibleType(bean, mismatch);
        if (!ignoreExtraProperties)
            matchingResult = matchingResult && hasNoExtraProperties(bean, mismatch);

        matchingResult = matchingResult && hasMatchingValues(bean, mismatch);

        return matchingResult;
    }

    private boolean hasMatchingValues(T item, Description mismatchDescription) {
        for (SamePropertyValuesAs.PropertyMatcher propertyMatcher : propertyMatchers) {
            if (!existingPropertiesToIgnore.contains(propertyMatcher.propertyName)) {
                if (!propertyMatcher.matches(item)) {
                    propertyMatcher.describeMismatch(item, mismatchDescription);
                    return false;
                }
            }
        }
        return true;
    }

    public static class PropertyMatcher extends DiagnosingMatcher<Object> {
        private final Method readMethod;
        private final Matcher matcher;
        private final String propertyName;

        public PropertyMatcher(PropertyDescriptor descriptor, Object expectedObject) {
            this.propertyName = descriptor.getDisplayName();
            this.readMethod = descriptor.getReadMethod();
            Object property;
            try {
                property = readProperty(readMethod, expectedObject);
            } catch (NoSuchMethodException e) {
                // assume property is null if it does not exist
                property = null;
            }
            if (property instanceof BigDecimal) {
                this.matcher = comparesEqualTo((BigDecimal) property);
            } else {
                this.matcher = equalTo(property);
            }
        }

        @Override
        public boolean matches(Object actual, Description mismatch) {
            Object actualValue;
            try {
                actualValue = readProperty(readMethod, actual);
            } catch (NoSuchMethodException e) {
                // ignoring match for properties that do not exist
                return true;
            }
            try {
                if (!matcher.matches(actualValue)) {
                    mismatch.appendText(propertyName + " ");
                    matcher.describeMismatch(actualValue, mismatch);
                    return false;
                }
            } catch (ClassCastException classCastException) {
                System.out.println("ClassCastException occurred for read method: " + readMethod.getName());
                throw classCastException;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(propertyName + ": ").appendDescriptionOf(matcher);
        }
    }

}

