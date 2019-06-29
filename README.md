# Event aggregation with Apache Spark

This project provides a sample application that aggregates events using Apache Spark.  

Let us consider an e-commerce site that sends information about what page a user is currently viewing and whether the cart is empty or not.  The idea is to try and analyze customer behavior as they are navigating through the site and make near-real time suggestions that might drive more sales, for example, if the customer has spent several minutes on a product and haven’t yet added it to the cart, perhaps show additional pertinent promotional offers based on customer’s buying history.  It is important to note that this sort of analysis is time-critical.  For the purposes of making real-time suggestions, the information collected might get stale as soon as customer starts looking at totally different products and/or leaves the site.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You will need JDK 1.8 to run this application.

### Running

Compile and build the JAR
```
./gradlew spDemo
```

Run the application

```
java -jar build/libs/spDemo.jar
```

Send some sample events

```
nc -l 3333
```

and then type events in the format

```
timestamp, customer_id, product_url, isProductInCart; where
timestamp: timestamp of event in epoch
customer_id: what customer this event is for
product_url: the relative URL of the product
isProductInCart: whether the customer has added the product into the cart
```

such as

```
1561323510, 1, /product/flower_pot_1,true
1561323510, 2, /product/flower_pot_1,false
1561323510, 3, /product/flower_pot_2,true
```

Then open your output text file, perhaps in a web browser, and refresh that page to see more stats come in.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details
