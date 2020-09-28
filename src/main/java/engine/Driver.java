package engine;

import model.OrderBook;

public class Driver {
    public static void main(final String[] args) {
        OrderBook obook = new OrderBook("ENZO");
        System.out.println("Created OrderBook");
        System.out.println(obook.toString());

    }
}