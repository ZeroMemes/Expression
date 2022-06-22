package me.zero.expressions.expression;

/**
 * @author Brady
 * @since 6/22/2022
 */
public final class SimplificationContext {

    private SimplificationContext() {}

    public static class Builder {

        public SimplificationContext build() {
            return new SimplificationContext();
        }
    }
}
