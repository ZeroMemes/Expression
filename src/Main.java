import me.zero.expressions.expression.evaluate.ExpressionEngine;

/**
 * @author Brady
 * @since 6/20/2022
 */
public class Main {

    public static void main(String[] args) {
        var engine = new ExpressionEngine();

        System.out.println(engine.eval("g(x,y)=x^2y^2"));
        System.out.println(engine.eval("f(x,y,z)=xyz"));

        System.out.println(engine.eval("x=f(3,4,1)"));
        System.out.println(engine.eval("y=x^2"));
        System.out.println(engine.eval("g(2,2)f(3,3,3)"));
        System.out.println(engine.eval("-3^4"));
    }
}
