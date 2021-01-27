package pl.edu.pw.mini.java.xmlomat;

import org.w3c.dom.NamedNodeMap;
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
    public static Iterable<Node> iterable(final NamedNodeMap namedNodeMap) {
        return () -> new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < namedNodeMap.getLength();
            }

            @Override
            public Node next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return namedNodeMap.item(index++);
            }
        };
    }

    private static double[] parseRandomPattern(String pattern) throws InvalidRandomPattern {
        double[] vars = Arrays.stream(pattern.split(":")).mapToDouble(Double::parseDouble).toArray();
        if(vars.length == 0) throw new InvalidRandomPattern(pattern);
        double[] returnVars = new double[3];
        returnVars[0] = vars[0];
        returnVars[1] = vars.length<2?vars[0]:vars[1];
        returnVars[2] = vars.length<3?1:vars[2];
        return returnVars;
    }
    public static Double parseRandomNumber(String pattern) throws InvalidRandomPattern {
        double[] vars = parseRandomPattern(pattern);
        return ThreadLocalRandom.current().nextInt((int) Math.round(vars[0]/vars[2]), (int) Math.round(vars[1]/vars[2]+1))*vars[2];
    }
    public static String stringifyRandomNumber(String pattern) throws InvalidRandomPattern {
        double[] vars = parseRandomPattern(pattern);
        Double val = parseRandomNumber(pattern);
        if(vars[2] == Math.floor(vars[2])) return String.valueOf(Math.round(val));
        return String.valueOf(val);
    }


}

class InvalidRandomPattern extends Exception
{
    public InvalidRandomPattern(String message)
    {
        super(message);
    }
}