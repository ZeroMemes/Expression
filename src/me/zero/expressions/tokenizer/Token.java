package me.zero.expressions.tokenizer;

import me.zero.expressions.provider.Operator;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Brady
 * @since 6/20/2022
 */
public record Token(Type type, String value) {

    public Token(Operator operator) {
        this(Type.OPERATOR, operator.name());
    }

    public double doubleValue() {
        return Double.parseDouble(this.value);
    }

    public Operator operator() {
        if (this.type != Type.OPERATOR) {
            return null;
        }
        return Operator.getByName(this.value).orElseThrow();
    }

    public int precedence() {
        var op = this.operator();
        return op != null ? op.getPrecedence() : 0;
    }

    public Operator.Associativity associativity() {
        var op = this.operator();
        return op != null ? op.getAssociativity() : null;
    }

    public Token toUnary() {
        var op = this.operator();
        return op != null ? new Token(op.toUnary()) : null;
    }

    public Token hintArgs(int args) {
        if (this.type != Type.FUNCTION) {
            throw new UnsupportedOperationException("Argument hint can only be applied to FUNCTION type");
        }
        return new Token(this.type, this.value + "/" + args);
    }

    public String functionName() {
        if (this.type != Type.FUNCTION) {
            throw new UnsupportedOperationException("Function name can only be resolved for FUNCTION type");
        }
        return this.value.split("/")[0];
    }

    public int functionArgs() {
        if (this.type != Type.FUNCTION) {
            throw new UnsupportedOperationException("Function args can only be resolved for FUNCTION type");
        }
        return Integer.parseInt(this.value.split("/")[1]);
    }

    public enum Type {
        LITERAL(true, ch -> ch == '.' || (ch >= '0' && ch <= '9')),
        VARIABLE(true, ch -> (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')),
        OPERATOR(false, Operator::isOperator),
        FUNCTION(true, ch -> false), // Always fail because this is contextually generated using VARIABLE tokens
        LEFT_PARENTHESIS(false, ch -> ch == '('),
        RIGHT_PARENTHESIS(false, ch -> ch == ')'),
        ARGUMENT_SEPARATOR(false, ch -> ch == ','),
        EQUALS(false, ch -> ch == '=');

        private final boolean buffered;
        private final Predicate<Character> chars;

        Type(boolean buffered, Predicate<Character> chars) {
            this.buffered = buffered;
            this.chars = chars;
        }

        public final boolean acceptsChar(char ch) {
            return this.chars.test(ch);
        }

        public final boolean isBuffered() {
            return this.buffered;
        }

        public static Optional<Type> getType(char ch) {
            for (var type : values()) {
                if (type.acceptsChar(ch)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }
}
