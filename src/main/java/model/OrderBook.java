package model;

public class OrderBook {
    private final String symbol;
    private final OrderQueue[] offers;     //we can use bit shifting to calculate index and avoid conditional branching

    public OrderBook(final String symbol) {
        this.symbol = symbol;
        this.offers = new OrderQueue[2];
        this.offers[0] = new OrderQueue(511); //bids
        this.offers[1] = new OrderQueue(511); //asks
    }

    public void processOrder(Order order) {
        final int idx = order.getSide() >>> 31; //will be 0 if side == 1, or 1 if side == -1
        final long orderQuantity = order.getQuantity();
        final double avgFillPrice = this.offers[idx^1].fillOrder(order);
        final long quantityFilled = orderQuantity - order.getQuantity();
        if(quantityFilled > 0) {
            //TODO: report trade, other stuff
        }
        if(quantityFilled < orderQuantity) {
            this.offers[idx].add(order);
        } //else trade was fully filled
    }

    public String toString() {
        Order[] bidArray = this.offers[0].asArrayCopy();
        Order[] askArray = this.offers[1].asArrayCopy();
        final StringBuilder book = new StringBuilder("Order Book:\n");

        int i=0;
        int j=0;

        while(i < bidArray.length && j < askArray.length) {
            book.append("\t" + bidArray[i++].toString(true) + "  -  " + askArray[j++].toString(false));
        }
        while(i < bidArray.length) {
            book.append("\t" + bidArray[i++].toString(true) + "  -  ");
        }
        while(j < askArray.length) {
            book.append("\t\t\t\t\t\t\t\t\t\t-  " + askArray[j++].toString(false));
        }
        return book.toString();
    }
}
