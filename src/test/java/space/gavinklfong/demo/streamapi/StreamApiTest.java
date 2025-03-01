package space.gavinklfong.demo.streamapi;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lombok.extern.slf4j.Slf4j;
import space.gavinklfong.demo.streamapi.models.Customer;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

@Slf4j
@DataJpaTest
public class StreamApiTest {

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private ProductRepo productRepo;

	@Test
	@DisplayName("Obtain a list of product with category = \"Books\" and price > 100")
	public void exercise1() {
		List<Product> result = productRepo.findAll()
		.stream()
		.filter(p -> p.getCategory().equalsIgnoreCase("Books"))
		.filter(p -> p.getPrice() > 100)
		.collect(Collectors.toList());

		result.forEach(System.out::println);
	}



	@Test
	@DisplayName("Obtain a list of order with product category = \"Baby\"")
	public void exercise2() {
		List<Order> result = orderRepo.findAll().stream().filter(o -> o.getProducts()
				.stream().anyMatch(p -> p.getCategory().equalsIgnoreCase("Baby")))
				.collect(Collectors.toList());

		result.forEach(System.out::println);


	}

	@Test
	@DisplayName("Obtain a list of product with category = “Toys” and then apply 10% discount\"")
	public void exercise3() {
		List<Product> result = productRepo.findAll().stream().filter(p ->
				p.getCategory().equalsIgnoreCase("Toys"))
				.map(p -> p.withPrice(p.getPrice()* 0.9)).collect(Collectors.toList());

		result.forEach(System.out::println);

	}

	@Test
	@DisplayName("Obtain a list of products ordered by customer of tier 2 between 01-Feb-2021 and 01-Apr-2021")
	public void exercise4() {
		List<Product> result = orderRepo.findAll().stream().filter(o -> o.getCustomer().getTier() == 2)
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 2, 1)) >= 0)
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 4, 1)) <= 0)
				.flatMap(o -> o.getProducts().stream())
				.distinct()
				.collect(Collectors.toList());

		result.forEach(System.out::println);
	}

	@Test
	@DisplayName("Get the cheapest products of \"Books\" category")
	public void exercise5() {
		Optional<Product> result = productRepo.findAll().stream().filter(p -> p.getCategory().equalsIgnoreCase("Books"))
				.min(Comparator.comparing(Product::getPrice));

		System.out.println(result);

	}

	@Test
	@DisplayName("Get the 3 most recent placed order")
	public void exercise6() {
		List<Order> result = orderRepo.findAll().stream().sorted(Comparator.comparing(Order::getOrderDate)
				.reversed())
				.limit(3).collect(Collectors.toList());

		result.forEach(System.out::println);
	}

	@Test
	@DisplayName("Get a list of products which was ordered on 15-Mar-2021")
	public void exercise7() {
		List<Product> result = orderRepo.findAll().stream()
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 3, 15)) == 0)
				.flatMap(o -> o.getProducts().stream())
				//returns duplicates without distinct
				.distinct()
				.collect(Collectors.toList());

		result.forEach(System.out::println);

	}

	@Test
	@DisplayName("Calculate the total lump of all orders placed in Feb 2021")
	public void exercise8() {
		Double result = orderRepo.findAll().stream()
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 2, 1)) >= 0)
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 2, 28)) <= 0)
				.flatMap(o -> o.getProducts().stream())
				.mapToDouble(Product::getPrice)
				.sum();

		System.out.println(result);

	}

	@Test
	@DisplayName("Calculate the average price of all orders placed on 15-Mar-2021")
	public void exercise9() {
		Double result = orderRepo.findAll().stream()
				.filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 3, 15)) == 0)
				.flatMap(o -> o.getProducts().stream())
				.mapToDouble(Product::getPrice)
				.average().getAsDouble();

		System.out.println(result);
	}

	@Test
	@DisplayName("Obtain statistics summary of all products belong to \"Books\" category")
	public void exercise10() {
		DoubleSummaryStatistics results = productRepo.findAll().stream()
				.filter(p -> p.getCategory().equalsIgnoreCase("Books"))
				.mapToDouble(Product::getPrice)
				.summaryStatistics();

		System.out.println(results);

	}

	@Test
	@DisplayName("Obtain a mapping of order id and the order's product count")
	public void exercise11() {
		Map<Long, Integer> result = orderRepo.findAll().stream()
				.collect(Collectors.toMap(Order::getId, o -> o.getProducts().size()));

		System.out.println(result);

	}

	@Test
	@DisplayName("Obtain a data map of customer and list of orders")
	public void exercise12() {
		Map<Customer, List<Order>> result = orderRepo.findAll().stream()
				.collect(Collectors.groupingBy(Order::getCustomer));

		System.out.println(result);

	}


	@Test
	@DisplayName("Obtain a data map with order and its total price")
	public void exercise13() {
		Map<Order, Double> result = orderRepo.findAll().stream()
				.collect(Collectors.toMap(
						Function.identity(), o -> o.getProducts().stream()
								.mapToDouble(Product::getPrice).sum()
				));

		System.out.println(result);

	}


	@Test
	@DisplayName("Obtain a data map of product name by category")
	public void exercise14() {
		Map<String, List<String>> result = productRepo.findAll().stream()
				.collect(Collectors.groupingBy(
						Product::getCategory, Collectors.mapping(Product::getName, Collectors.toList())
				));

		System.out.println(result);

	}

	@Test
	@DisplayName("Get the most expensive product per category")
	void exercise15() {
		Map<String, Optional<Product>> result = productRepo.findAll().stream()
				.collect(Collectors.groupingBy(Product::getCategory,
						Collectors.maxBy(Comparator.comparing(Product::getPrice))));

		System.out.println(result);

	}
	


}
