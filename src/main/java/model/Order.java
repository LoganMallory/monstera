package model;

public class Order implements Comparable<Order>{
    private final String id;
    private final String symbol;
    private final int side;
    private long price;
    private long quantity;
    private long qprice;

    public Order(final String id, final String symbol, final int side, final long price, final long quantity) {
        this.id        = id;
        this.symbol    = symbol;
        this.side      = side;
        this.price     = price;
        this.quantity  = quantity;
        this.qprice    = this.side * this.price;
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

    public long getQuantity() {
        return this.quantity;
    }

    public void setQuantity(final long newQuantity) {
        this.quantity = newQuantity;
    }

    public void addQuantity(final long additionalQuantity) {
        this.quantity += additionalQuantity;
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
        return new Order(this.id, this.symbol, this.side, this.price, this.quantity);
    }

    @Override
    public int compareTo(Order otherOrder) {
        int priceCheck = Long.compare(this.qprice, otherOrder.qprice);                          //compare prices first, then quantity if prices were equal
        return priceCheck != 0 ? priceCheck : Long.compare(this.quantity, otherOrder.quantity);
    }

    public String toString() {
        return this.id + " [" + this.quantity + "] " + this.price;
    }

    public String toString(boolean leftAligned) {
        if(leftAligned) {
            return this.id + " [" + this.quantity + "] " + this.price;
        } else {
            return this.price + " [" + this.quantity + "] " + this.id;
        }
    }


}