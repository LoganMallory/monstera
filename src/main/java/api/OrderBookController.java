package api;

import com.fasterxml.jackson.annotation.JsonView;
import model.Order;
import model.OrderBook;
import model.Views;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
public class OrderBookController {
    private final Logger logger;
    private final OrderBook orderBook;

    public OrderBookController(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.logger    = LoggerFactory.getLogger(OrderBookController.class);
    }

    @JsonView(Views.OrderBook.class)
    @RequestMapping(method = RequestMethod.GET, value = "/orders")
    public OrderBook getOrderBok() {
        logger.info("GET /orders");
        return this.orderBook;
    }

    @JsonView(Views.Order.class)
    @RequestMapping(method = RequestMethod.GET, value = "/orders/{ids}")
    public Order[] getOrders(@PathVariable final String[] ids) {
        logger.info("GET /orders/{}", Arrays.toString(ids));
        final Order[] orders = new Order[ids.length];
        for(int i=0; i < ids.length; i++) {
            orders[i] = this.orderBook.getOrder(ids[i]);
        }
        return orders;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/orders")
    public Message placeOrder(@RequestBody Order order) {
        logger.info("POST /orders (order={})", order.toString());
        if(!order.getSymbol().equals(this.orderBook.getSymbol())) return new Message("Order rejected. Invalid symbol (" + order.getSymbol() + ") for order book with symbol " + this.orderBook.getSymbol());
        if(order.getSide() != -1 && order.getSide() != 1) return new Message("Order rejected. Invalid side (" + order.getSide() + "). Side must be -1 or 1");
        if(order.getPrice() < 0) return new Message("Order rejected. Invalid price (" + order.getPrice() + "). Price must be >= 0");
        if(order.getRemainingQuantity() < 1) return new Message("Order rejected. Invalid quantity (" + order.getRemainingQuantity() + "). Quantity must be > 0");
        this.orderBook.processOrder(order);
        return new Message("Order accepted. Id: " +  order.getId());
    }

}
