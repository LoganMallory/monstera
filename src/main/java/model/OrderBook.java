package model;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.Arrays;

public class OrderBook {
    @JsonView(Views.OrderBook.class)
    private final String symbol;
    private final OrderQueue[] offers;     //we can use bit shifting to calculate index and avoid conditional branching

    public OrderBook(final String symbol) {
        this.symbol = symbol;
        this.offers = new OrderQueue[2];
        this.offers[0] = new OrderQueue(511); //bids
        this.offers[1] = new OrderQueue(511); //asks
    }

    public String getSymbol() {
        return this.symbol;
    }

    @JsonView(Views.OrderBook.class)
    public Order[] getBids() {
        return this.offers[0].asArrayCopy();
    }

    @JsonView(Views.OrderBook.class)
    public Order[] getAsks() {
        return this.offers[1].asArrayCopy();
    }

    public void processOrder(Order order) {
        final int idx = order.getSide() >>> 31; //will be 0 if side == 1, or 1 if side == -1
        final long orderQuantity = order.getRemainingQuantity();
        final double avgFillPrice = this.offers[idx^1].fillOrder(order);
        final long quantityFilled = orderQuantity - order.getRemainingQuantity();
        if(quantityFilled > 0) {
            //TODO: report trade, other stuff
        }
        if(quantityFilled < orderQuantity) {
            this.offers[idx].add(order);
        } //else trade was fully filled
    }

    public Order getOrder(String id) {
        final int idx = (id.charAt(0) - '0') ^ 1; //first char in ID is order side (0 for ask, 1 for bid)
        return this.offers[idx & 1].getOrder(id); //& 1 makes sure idx is never out of bounds (avoids conditional branching too)
    }

    public String toString() {
        Order[] bidArray = this.offers[0].asArrayCopy();
        Order[] askArray = this.offers[1].asArrayCopy();
        Arrays.sort(bidArray);
        Arrays.sort(askArray);

        final StringBuilder book = new StringBuilder("Order Book:\n");

        int i=0;
        int j=0;

        while(i < bidArray.length && j < askArray.length) {
            book.append("\t").append(bidArray[i++].toString(true)).append("  -  ").append(askArray[j++].toString(false));
        }
        while(i < bidArray.length) {
            book.append("\t").append(bidArray[i++].toString(true)).append("  -  ");
        }
        while(j < askArray.length) {
            book.append("\t\t\t\t\t\t\t\t\t\t-  ").append(askArray[j++].toString(false));
        }
        return book.toString();
    }
}