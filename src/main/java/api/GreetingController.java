package api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController //shorthand for @Controller, @ResponseBody
public class GreetingController {
    private static final String template = "Hello %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method= RequestMethod.GET, value = "/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue = "unknown user") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
