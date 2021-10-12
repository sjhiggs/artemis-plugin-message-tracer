
public class Consumer extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("amqp:queue:example?exchangePattern=InOnly")
            .to("log:info");
    }
}
