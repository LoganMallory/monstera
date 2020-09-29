package model;

import api.OrderBookController;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

//TODO: enforce limits on number of client orders (per client, and total)
public class OrderBook {
    @JsonView(Views.OrderBook.class)
    private final String symbol;
    private final OrderQueue[] offers;     //we can use bit shifting to calculate index and avoid conditional branching
    private final int pricePrecision;      //2 = cents, 3 = tenth of a cent
    private AtomicLong lastTradedPrice;
    private final Logger logger;

    public OrderBook(final String symbol) {
        this.symbol          = symbol;
        this.offers          = new OrderQueue[2];
        this.offers[0]       = new OrderQueue(4095); //bids
        this.offers[1]       = new OrderQueue(4095); //asks
        this.pricePrecision  = 2;
        this.lastTradedPrice = new AtomicLong(-1);
        this.logger          = LoggerFactory.getLogger(OrderBookController.class);
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

    @JsonView(Views.OrderBook.class)
    public long getLastTradedPrice() {
        return this.lastTradedPrice.get();
    }

    public void processOrder(Order order) {
        final int idx             = order.getSide() >>> 31; //will be 0 if side == 1, or 1 if side == -1
        final long orderQuantity  = order.getRemainingQuantity();
        final double avgFillPrice = this.offers[idx^1].fillOrder(order, this.lastTradedPrice);
        final long quantityFilled = orderQuantity - order.getRemainingQuantity();
        if(quantityFilled > 0) {
            logger.info("Order " + order.getId() + " filled: " + avgFillPrice + " x " + quantityFilled);
            //TODO: report trade (how to get other order's id?), other stuff
        }
        if(quantityFilled < orderQuantity && order.getPrice() > 0) { //order was only partially filled, add remaining to queue (except market orders)
            this.offers[idx].add(order);
        } //else order was fully filled, so dont put it in the queue
    }

    public int updateOrder(final String id, final long price, final long quantity) {
        final int idx     = ((id.charAt(0) - '0') ^ 1) & 1; //first char in ID is order side (0 for ask, 1 for bid), & 1 keeps idx in bounds
        final Order order = this.offers[idx].getOrder(id);
        if(order == null) return -1;
        this.offers[idx].remove(id); //remove the order first
        order.setPrice(price);
        order.setQuantity(quantity);
        this.processOrder(order);    //match order against opposite side, and place in heap if not fully filled
        return 0;
    }

    public Order getOrder(String id) {
        final int idx = ((id.charAt(0) - '0') ^ 1) & 1; //first char in ID is order side (0 for ask, 1 for bid), & 1 keeps idx in bounds
        return this.offers[idx].getOrder(id);
    }
}