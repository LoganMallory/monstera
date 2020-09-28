package api;

import model.OrderBook;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

@Repository
public class OrderBookService {
    @Bean
    public OrderBook createOrderBook() {
        return new OrderBook("SPY");
    }
}
