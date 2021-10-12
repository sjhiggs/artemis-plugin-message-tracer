import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

// camel-k: language=java

public class Producer extends RouteBuilder {

  private AtomicInteger counter = new AtomicInteger(0);

  @Override
  public void configure() throws Exception {
    from("timer:foo?period=1000").setBody().simple("Hello Camel K").process(new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader("trackingid", String.valueOf(counter.incrementAndGet()));
      }
    })
    .log("the current header is ${in.header.trackingid}")
    .to("amqp:queue:example?exchangePattern=InOnly");
  }
}
