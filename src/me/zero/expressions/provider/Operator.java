package me.zero.expressions.provider;

import me.zero.expressions.expression.Expression;
import me.zero.expressions.expression.ast.AddSubtract;
import me.zero.expressions.expression.ast.Exponent;
import me.zero.expressions.expression.ast.MultiplyDivide;

import java.util.List;
import java.util.Optional;

import static me.zero.expressions.provider.Operator.Associativity.*;

/**
 * @author Brady
 * @since 6/20/2022
 */
public enum Operator implements ExpressionProvider {
    IDENTITY(3) {
        @Override
        public Expression apply(Expression... args) {
            return args[0];
        }
    },
    NEGATE(3) {
        @Override
        public Expression apply(Expression... args) {
            return new AddSubtract(List.of(), List.of(args[0]));
        }
    },
    ADD('+', 1, LEFT, IDENTITY) {
        @Override
        public Expression apply(Expression... args) {
            return new AddSubtract(List.of(args[0], args[1]), List.of());
        }
    },
    SUBTRACT('-', 1, LEFT, NEGATE) {
        @Override
        public Expression apply(Expression... args) {
            return new AddSubtract(args[0], args[1]);
        }
    },
    MULTIPLY('*', 2, LEFT) {
        @Override
        public Expression apply(Expression... args) {
            return new MultiplyDivide(List.of(args[0], args[1]), List.of());
        }
    },
    DIVIDE('/', 2, LEFT) {
        @Override
        public Expression apply(Expression... args) {
            return new MultiplyDivide(args[0], args[1]);
        }
    },
    EXP('^', 4, RIGHT) {
        @Override
        public Expression apply(Expression... args) {
            return new Exponent(args[0], args[1]);
        }
    };

    private static final char NULL = '\0';

    private final char symbol;
    private final int precedence;
    private final Associativity associativity;
    private final Operator asUnary;
    private final boolean isUnary;

    Operator(int precedence) {
        this(NULL, precedence, NONE, null, true);
    }

    Operator(char symbol, int precedence, Associativity associativity) {
        this(symbol, precedence, associativity, null);
    }

    Operator(char symbol, int precedence, Associativity associativity, Operator asUnary) {
        this(symbol, precedence, associativity, asUnary, false);
    }

    Operator(char symbol, int precedence, Associativity associativity, Operator asUnary, boolean isUnary) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.associativity = associativity;
        this.asUnary = asUnary;
        this.isUnary = isUnary;
    }

    public final char getSymbol() {
        return this.symbol;
    }

    public final int getPrecedence() {
        return this.precedence;
    }

    public final Associativity getAssociativity() {
        return associativity;
    }

    public final Operator toUnary() {
        return this.asUnary;
    }

    public final boolean isUnary() {
        return this.isUnary;
    }

    public static Optional<Operator> getByName(String name) {
        for (Operator o : values()) {
            if (o.name().equals(name)) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    public static Optional<Operator> getBySymbol(char symbol) {
        for (Operator o : values()) {
            if (o.getSymbol() != NULL && o.getSymbol() == symbol) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    public static boolean isOperator(char symbol) {
        return getBySymbol(symbol).isPresent();
    }

    public enum Associativity {
        LEFT,
        RIGHT,
        NONE
    }
}
