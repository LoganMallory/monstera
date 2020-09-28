package model;

import java.util.Arrays;
import java.util.HashMap;


public class OrderQueue {
    public static final boolean DEBUG = false;
    private final Order[] heap;
    private final HashMap<String, Integer> indexes;
    private int size;
    private long depth;
    private double avgPrice; //prices are in long format (hundredths of a cent), but average should be double

    public OrderQueue(int capacity) {
        if(DEBUG) System.out.println("OrderQueue(" + capacity + ")");
        this.heap     = new Order[capacity];
        this.indexes  = new HashMap<String, Integer>(capacity, 1.0f);
        this.size     = 0;
        this.depth    = 0;
        this.avgPrice = 0;
    }

    public int getSize() {
        return this.size;
    }

    public int getCapacity() {
        return this.heap.length;
    }

    public double fillOrder(final Order incomingOrder) {
        //TODO: handle market orders
        final long mprice                = incomingOrder.getQprice() * -1;  //multiply queue price by -1 to make price logic work for both bids & asks
        double avgFillPrice              = 0;
        long incomingQuantityFilledSoFar = 0;

        Order bestOffer = this.heap[0];                      //grab reference to best offer (null if heap is empty)
        // while incoming order still has quantity left to fill and price point works with best offer
        while(incomingOrder.getRemainingQuantity() > 0 && this.size > 0 && bestOffer.getQprice() >= mprice) {
            long incomingQuantityFilledOnIteration = this.fillRootOffer(incomingOrder.getRemainingQuantity());   //fill order and return quantity filled, update average fill price
            avgFillPrice = (avgFillPrice*incomingQuantityFilledSoFar + bestOffer.getPrice()*incomingQuantityFilledOnIteration) / (incomingQuantityFilledSoFar + incomingQuantityFilledOnIteration);
            incomingQuantityFilledSoFar += incomingQuantityFilledOnIteration;
            incomingOrder.addQuantity(incomingQuantityFilledOnIteration*-1); //update remaining quantity
            bestOffer = this.heap[0];                                //grab next best offer (could be same order, but then while loop will exit)
        }
        return avgFillPrice;
    }

    public Order peek() {
        if(DEBUG) System.out.println("peek()");
        return this.heap[0];                                         //return a reference to the top priority element
    }

    public Order poll() {
        if(DEBUG) System.out.println("poll()");
        final Order best = this.heap[0];                                   //get reference to best order
        this.remove(0);
        return best;
    }

    public void add(final Order order) {
        //TODO: in the case where new order has same price & quantity as existing order, existing order gets precedence (change sift down method comparison)
        if(DEBUG) System.out.println("add(" + order.toString() + ")");
        this.heap[this.size++] = order;                              //put new order in last leaf
        this.indexes.put(order.getId(), this.size-1);                //add to index map
        this.siftUp(this.size-1);                                 //sift up until in place
        this.updateMetrics(order.getPrice(), order.getRemainingQuantity());   //update average, increase depth
    }

    private void remove(final int i) {
        if(DEBUG) System.out.println("remove(" + i + ")");
        this.swap(i, --this.size);                                   //swap node with last leaf
        this.siftDown(i);                                            //heapify the tree
        final Order order = this.heap[this.size];
        this.updateMetrics(order.getPrice(), order.getRemainingQuantity()*-1);  //update average, decrease depth

    }

    public int remove(final String id) {
        if(DEBUG) System.out.println("remove(" + id + ")");
        final Integer idx = this.indexes.get(id);
        if(idx == null) return -1;
        this.remove(idx);
        return 0;
    }

    public int update(final String id, final long newPrice, final long newQuantity) {
        //TODO: order updates should actually remove and then add new order
        //because order update should also trigger matching against other queue (which might mean order is filled)
        if(DEBUG) System.out.println("update(" + id + ", " + newPrice + ", " + newQuantity + ")");
        final Integer idx = this.indexes.get(id);                    //get index of order
        if(idx == null) return -1;                                   //make sure its not null (order exists)
        final Order order       = this.heap[idx];
        final Order ogOrderCopy = order.dcopy();                     //make copy of original order for comparison later
        order.setPrice(newPrice);                                    //set new price & quantity (& internally, qprice)
        order.setQuantity(newQuantity);
        final int comparison = order.compareTo(ogOrderCopy);         //compare new and original order
        if (comparison >= 0) {                                       //new order is better (or the same possibly)
            this.siftUp(idx);                                        //so sift up
        } else {                                                     //else new order is worse
            this.siftDown(idx);                                      //so sift down
        }
        this.updateMetrics(newPrice, newQuantity - ogOrderCopy.getRemainingQuantity()); //update average, update depth
        return 0;
    }

    public long fillRootOffer(final long incomingOrderQuantity) {
        long incomingQuantityFilled = incomingOrderQuantity;         //assume only a partial fill of root order
        long metricQuantity = incomingOrderQuantity*-1;

        final Order rootOrder = this.heap[0];
        final long rootQuantity = rootOrder.getRemainingQuantity();

        rootOrder.addQuantity(incomingOrderQuantity*-1);            //update quantity of root node

        if(rootOrder.getRemainingQuantity() <= 0) {                           //if root order was completely filled
            this.swap(0, --this.size);                            //remove the node (dont use remove() method because of updateMetrics logic)
            incomingQuantityFilled = rootQuantity;                   //quantity filled is equal to original root quantity
            metricQuantity = rootQuantity*-1;                        //use offer quantity for metric update
        }

        this.siftDown(0);                                         //heapify at root
        this.updateMetrics(rootOrder.getPrice(), metricQuantity);    //update average price, depth
        return incomingQuantityFilled;
    }

    private void siftUp(int i) {
        if(DEBUG) System.out.println("\tsiftUp(" + i + ")");
        while(i > 0) {                                               //while not root node
            int parent = (i - 1) >>> 1;
            if(this.heap[i].compareTo(this.heap[parent]) > 0) {      //if child greater than parent node
                this.swap(i, parent);                                //then swap nodes
                i = parent;
            } else {                                                 //otherwise parent was greater
                return;                                              //so exit function
            }
        }
    }

    private void siftDown(int i) {
        if(DEBUG) System.out.println("\tsiftDown(" + i + ")");
        int leftChild, rightChild, biggestChild;
        while(i < this.size >>> 1) {                                 //while not a leaf node
            leftChild  = (i << 1) + 1;
            rightChild = leftChild + 1;
            if(rightChild >= this.size) {                            //if right child is out of bounds
                biggestChild = leftChild;                            //left child must be biggest child
            } else {                                                 //otherwise compare left and right child
                biggestChild = this.heap[leftChild].compareTo(this.heap[rightChild]) >= 0 ? leftChild : rightChild;
            }
            if(this.heap[i].compareTo(this.heap[biggestChild]) >= 0) { //if parent is greater than or equal to largest child
                return;                                                //then heap invariant is ok, so return
            } else {
                this.swap(i, biggestChild);                            //otherwise parent was less than child
                i = biggestChild;                                      //so swap with biggest child
            }
        }
    }

    private void swap(final int i, final int j) {
        if(DEBUG) System.out.println("\tswap(" + i + ", " + j + ")");
        final Order x = this.heap[i];                                      //grab order references
        final Order y = this.heap[j];

        this.heap[i] = y;                                            //swap order references
        this.heap[j] = x;

        this.indexes.put(x.getId(), j);                              //update indexes
        this.indexes.put(y.getId(), i);
    }

    private void updateMetrics(final long price, final long quantity) {
        //TODO: https://fanf2.user.srcf.net/hermes/doc/antiforgery/stats.pdf (I tested this, but it loses some precision though after enough iterations)
        this.avgPrice = ((this.avgPrice * this.depth) + (price * quantity)) / (this.depth + quantity); //update weighted average (could overflow, but even 1B * 1B < 2**63 - 1)
        this.depth   += quantity;                                                                      //update depth (quantity may be negative)
    }

    public Order getOrder(final String id) {
        final Integer idx = this.indexes.get(id);                          //get index of order
        if(idx == null) return null;                                       //make sure its not null (order exists)
        return this.heap[idx];
    }
    public Order[] asArrayCopy() {                                             //IDs WILL BE DIFFERENT
        final Order[] orderArr = new Order[this.size];                         //array to hold copied orders
        for(int i=0; i < this.size; i++) orderArr[i] = this.heap[i].dcopy();   //deep copy the orders into the new array (edits on these orders don't affect heap)
        return orderArr;
    }
}