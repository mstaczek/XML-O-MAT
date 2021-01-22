package pl.edu.pw.mini.java.xmlomat;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {
    public static Iterable<Node> iterable(final NodeList nodeList) {
        return () -> new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return nodeList.item(index++);
            }
        };
    }

    public static Double parseRandomNumber(String pattern) throws InvalidRandomPattern {
        double[] vars = Arrays.stream(pattern.split(":")).mapToDouble(Double::parseDouble).toArray();
        if(vars.length == 0) throw new InvalidRandomPattern(pattern);
        if(vars.length == 1) return vars[0];

        double step = 1;
        if(vars.length > 2) step = vars[2];
        return ThreadLocalRandom.current().nextInt((int) Math.round(vars[0]/step), (int) Math.round(vars[1]/step+1))*step;
    }


}

class InvalidRandomPattern extends Exception
{
    public InvalidRandomPattern(String message)
    {
        super(message);
    }
}