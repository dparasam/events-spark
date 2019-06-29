package sp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;


/**
 * The EventAnalyzer is a sample event analysis application.
 *
 * Let us consider an a e-commerce site that sends information about what page a user is currently
 * viewing and whether the cart is empty or not.  The idea is to try and analyze customer behavior
 * as they are navigating through the site and make near-real-time suggestions that might drive
 * more sales, for example, if the customer has spent several minutes on a product and haven’t yet
 * added it to the cart, perhaps show additional pertinent promotional offers based on customer’s
 * buying history.  It is important to note that this sort of analysis is time-critical.  For the
 * purposes of making real-time suggestions, the information collected might get stale as soon as
 * customer starts looking at totally different products and/or leaves the site.

 * For now, this is a simple application that:
 *   - Reads lines from a socket for performing streaming analytics.  In reality, this could
 *     come from other streaming platforms such as Kafka, AWS Kinesis, or such
 *   - Computes the top viewed product pages but not yet added into the cart by the customer,
 *     in the last predefined interval (a metric that could be useful to target promotions)
 *   - Writes the calculated statistics to a file on the local file system that gets refreshed
 *     every predefined time interval.  In reality, these results could be streamed into other systems
 *     for further analysis, processing, or storage, for instance into AWS Dynamo, HBase etc.
 *
 * To compile and run this program:
 *  - ./gradlew spDemo
 *  - java -jar build/libs/spDemo.jar
 *
 * Once you get this program up and running, you could ingest events into it by running:
 *      nc -l 3333
 * and then typing in events in the format:
 *      timestamp, customer_id, product_url, isProductInCart; where
 *      timestamp: timestamp of event in epoch
 *      customer_id: what customer this event is for
 *      product_url: the relative URL of the product
 *      isProductInCart: whether the customer has added the product into the cart
 *
 * Sample events:
 *      1561323510, 1, /product/flower_pot_1,true
 *      1561323510, 2, /product/flower_pot_1,false
 *      1561323510, 3, /product/flower_pot_2,true
 *
 * Then open your output text file, perhaps in a web browser, and refresh that page to see more stats come in.
 */
public class EventAnalyzer {
    private static final String HOST = "localhost";
    private static final int PORT = 3333;
    private static final String CHECKPOINT_DIR = "/tmp/sp";
    private static final String OUTPUT_DIR = "/tmp/sp";
    private static final Duration WINDOW_DURATION = new Duration(5000);
    private static final Duration SLIDE_DURATION = new Duration(5000);

    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf()
            .setMaster("local[*]")
            .setAppName("Top viewed unsold products");

        try (JavaStreamingContext context = new JavaStreamingContext(conf, WINDOW_DURATION)) {
            context.checkpoint(CHECKPOINT_DIR);
            JavaReceiverInputDStream<String> lines = context.socketTextStream(HOST, PORT);
            JavaDStream<UserEvent> events = lines.flatMap(
                new FlatMapFunction<String, UserEvent>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Iterator<UserEvent> call(String line) {
                    String[] fields = line.split(",");
                    if (fields.length < 4) {
                        return new ArrayList<UserEvent>().iterator();
                    }
                    return Arrays.asList(new UserEvent(
                        Long.valueOf(fields[0].trim()),
                        fields[1].trim(),
                        fields[2].trim(),
                        Boolean.valueOf(fields[3].trim()))).iterator();
                }
            });

            JavaDStream<UserEvent> cartEmpty = events.filter(e -> e.getIsCartEmpty());
            JavaPairDStream<String, Long> result = cartEmpty
                .map(new Function<UserEvent, String>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String call(UserEvent event) throws Exception {
                        return event.getPage();
                    }
                })
                .countByValueAndWindow(WINDOW_DURATION, SLIDE_DURATION);

            result.foreachRDD(r -> {
                r.coalesce(1).saveAsTextFile(OUTPUT_DIR);
            });

            context.start();
            context.awaitTermination();
        }
    }
}
