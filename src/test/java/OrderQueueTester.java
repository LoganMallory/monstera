import model.Order;
import model.OrderQueue;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class OrderQueueTester {

    @Test
    public void constructorShouldCreateEmptyHeap() {
        /* OrderQueue(int capacity) should create a heap of length <capacity> and size = 0 */
        OrderQueue oq = new OrderQueue(0);
        Assert.assertEquals(0, oq.getCapacity());
        Assert.assertEquals(0, oq.getSize());

        oq = new OrderQueue(1);
        Assert.assertEquals(1, oq.getCapacity());
        Assert.assertEquals(0, oq.getSize());

        oq = new OrderQueue(512);
        Assert.assertEquals(512, oq.getCapacity());
        Assert.assertEquals(0, oq.getSize());
    }

    @Test
    public void AddingNodeToEmptyTreeShouldCreateRoot() {
        /* OrderQueue.add(order) should add a new node at the root when the heap is empty */
        OrderQueue oq = new OrderQueue(512);
        Order order = new Order("abc", "SPY", 1, 1005, 500);
        oq.add(order);

        Assert.assertEquals(1, oq.getSize());
    }

    @Test
    public void PeekingQueueShouldReturnReferenceAndNotDelete() {
        /* OrderQueue.peek() should return a reference to the top priority node without removing it */
        OrderQueue oq = new OrderQueue(512);
        Order order = new Order("abc", "SPY", 1, 1005, 500);
        oq.add(order);

        Order topOrder = oq.peek();
        Assert.assertEquals(1, oq.getSize());
    }

    @Test
    public void PollingShouldRemoveRootNode() {
        /* OrderQueue.poll() should return a reference to the top priority node and remove it from the heap */
        OrderQueue oq = new OrderQueue(512);
        Order order   = new Order("abc", "SPY", 1, 1005, 500);
        oq.add(order);

        Order topOrder = oq.poll();
        Assert.assertEquals(0, oq.getSize());
    }

    @Test
    public void RandomTestingAgainstJavaUtilPriorityTesting() {
        /* OrderQueue.remove() should return a reference to the top priority node and remove it from the heap */
        OrderQueue orderqueue              = new OrderQueue(512);
        PriorityQueue<Order> priorityqueue = new PriorityQueue<Order>(512, Collections.reverseOrder());
        long price, quantity;

        for(int i=0; i < 500; i++) {
            //generate random price between [1 and 1000]
            price = ThreadLocalRandom.current().nextLong(1, 1000 + 1);
            //generate a random quantity between 1 and 5000
            quantity = ThreadLocalRandom.current().nextLong(1, 5000 + 1);
            Order order = new Order(Integer.toString(i), "SPY", 1, price, quantity);
            orderqueue.add(order);
            priorityqueue.add(order.dcopy());
        }
        for(int i=0; i < 500; i++) {
            Order oqOrder = orderqueue.poll();
            Order pqOrder = priorityqueue.poll();
            Assert.assertEquals(oqOrder.getId(), pqOrder.getId());
        }
    }
}

