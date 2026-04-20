package java.util;

import java.util.function.Consumer;

/**
 * Implementation of Optional from
 * https://medium.com/@rmzoni/optional-implementation-for-java-7-eb70aed7cd3c
 */
public class Optional<T> {

    private static final Optional<Null> EMPTY = new Optional<>(Null.INSTANCE);
    private T value;

    protected Optional(T value) {
        this.value = value;
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<T>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    public static <T> Optional<T> ofNullable(T value) {
        if (value == null) {
            return empty();
        } else {
            return of(value);
        }
    }

    public boolean isPresent() {
        return !(this.value == null || this == EMPTY);
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if a value is present
     * @throws NullPointerException if value is present and the given action is
     *         {@code null}
     */
    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is
     *        present
     * @throws NullPointerException if a value is present and the given action
     *         is {@code null}, or no value is present and the given empty-based
     *         action is {@code null}.
     * @since 9
     */
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public T get() throws NoSuchElementException {
        if (isPresent()) {
            return value;
        } else {
            throw new NoSuchElementException();
        }
    }

    public T orElse(T other) {
        if (isPresent()) {
            return value;
        } else {
            return other;
        }
    }

    private static class Null {
        private static final Null INSTANCE = new Null();

        private Null() {
            // Private constructor
            super();
        }
    }
}