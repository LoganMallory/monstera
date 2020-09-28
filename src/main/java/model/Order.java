package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.UUID;

@JsonIgnoreProperties({ "qprice" })
public class Order implements Comparable<Order> {
    @JsonView(Views.Order.class)
    private final String id;
    private final String symbol;
    @JsonView(Views.Order.class)
    private final int side;
    @JsonView({Views.OrderBook.class, Views.Order.class})
    private long price;
    @JsonView({Views.OrderBook.class, Views.Order.class})
    private long remainingQuantity;
    private long qprice;

    public Order(final String symbol, final int side, final long price, final long quantity) {
        this.id                = ((side >>> 31) ^ 1) + UUID.randomUUID().toString();
        this.symbol            = symbol;
        this.side              = side;
        this.price             = price;
        this.remainingQuantity = quantity;
        this.qprice            = this.side * this.price;
    }

    public String getId() {
        return this.id;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public long getPrice() {
        return this.price;
    }

    public long getRemainingQuantity() {
        return this.remainingQuantity;
    }

    public void setQuantity(final long newQuantity) {
        this.remainingQuantity = newQuantity;
    }

    public void addQuantity(final long additionalQuantity) {
        this.remainingQuantity += additionalQuantity;
    }

    public int getSide() { return this.side; }

    public long getQprice() {
        return this.qprice;
    }

    public void setPrice(final long newPrice) {
        this.price  = newPrice;
        this.qprice = this.price * this.side;
    }

    public Order dcopy() {
        //id will be different
        return new Order(this.symbol, this.side, this.price, this.remainingQuantity);
    }

    @Override
    public int compareTo(Order otherOrder) {
        int priceCheck = Long.compare(this.qprice, otherOrder.qprice);                          //compare prices first, then quantity if prices were equal
        return priceCheck != 0 ? priceCheck : Long.compare(this.remainingQuantity, otherOrder.remainingQuantity);
    }

    public String toString() {
        return this.id + " " + this.symbol + " " + this.price + " x " + this.remainingQuantity;
    }

    public String toString(boolean leftAligned) {
        if(leftAligned) {
            return "[" + this.remainingQuantity + "] " + this.price;
        } else {
            return this.price + " [" + this.remainingQuantity + "]";
        }
    }
}