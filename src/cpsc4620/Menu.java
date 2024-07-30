package cpsc4620;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the methods for each of the menu options.
 * 
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 * 
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {
		

		System.out.println("Welcome to Pizzas-R-Us!");
		
		int menu_option = 0;

		// present a menu of options and take their selection
		
		PrintMenu();
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
			case 1:// enter order
				EnterOrder();
				break;
			case 2:// view customers
				viewCustomers();
				break;
			case 3:// enter customer
				EnterCustomer();
				break;
			case 4:// view order
				// open/closed/date
				ViewOrders();
				break;
			case 5:// mark order as complete
				MarkOrderAsComplete();
				break;
			case 6:// view inventory levels
				ViewInventoryLevels();
				break;
			case 7:// add to inventory
				AddInventory();
				break;
			case 8:// view reports
				PrintReports();
				break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException 
	{

		/*
		 * EnterOrder should do the following:
		 * 
		 * Ask if the order is delivery, pickup, or dinein
		 *   if dine in....ask for table number
		 *   if pickup...
		 *   if delivery...
		 * 
		 * Then, build the pizza(s) for the order (there's a method for this)
		 *  until there are no more pizzas for the order
		 *  add the pizzas to the order
		 *
		 * Apply order discounts as needed (including to the DB)
		 * 
		 * return to menu
		 * 
		 * make sure you use the prompts below in the correct order!
		 */

		 // User Input Prompts...
		System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		int orderType = Integer.parseInt(reader.readLine());
		String orderTypeString;
		if (orderType == 1) {
			orderTypeString = DBNinja.dine_in;
		} else if (orderType == 2) {
			orderTypeString = DBNinja.pickup;
		} else {
			orderTypeString = DBNinja.delivery;
		}

		
		System.out.println("Is this order for an existing customer? Answer y/n: ");
		String isExistingCustomer = reader.readLine();
		Customer customer;
		if (isExistingCustomer.equalsIgnoreCase("y")) {
			System.out.println("Here's a list of the current customers: ");
			viewCustomers();
			System.out.println("Which customer is this order for? Enter ID Number:");
			int customerID = Integer.parseInt(reader.readLine());
			customer = DBNinja.findCustomerByPhone(String.valueOf(customerID));
		} else {
			EnterCustomer();
			customer = DBNinja.findCustomerByPhone("0000000000");
		}

		Order order;
		if (orderTypeString.equals(DBNinja.dine_in)) {
			System.out.println("What is the table number for this order?");
			int tableNumber = Integer.parseInt(reader.readLine());
			order = new DineinOrder(0, customer.getCustID(), "", 0.0, 0.0, 0, tableNumber);
		} else if (orderTypeString.equals(DBNinja.pickup)) {
			order = new PickupOrder(0, customer.getCustID(), "", 0.0, 0.0, 0, 0);
		} else {
			System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
			String houseNumber = reader.readLine();
			System.out.println("What is the Street for this order? (e.g., Smile Street)");
			String street = reader.readLine();
			System.out.println("What is the City for this order? (e.g., Greenville)");
			String city = reader.readLine();
			System.out.println("What is the State for this order? (e.g., SC)");
			String state = reader.readLine();
			System.out.println("What is the Zip Code for this order? (e.g., 20605)");
			String zip = reader.readLine();
			String address = houseNumber + " " + street + ", " + city + ", " + state + " " + zip;
			order = new DeliveryOrder(0, customer.getCustID(), "", 0.0, 0.0, 0, address);
		}

		ArrayList<Pizza> pizzas = new ArrayList<>();
		while (true) {
			System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			String response = reader.readLine();
			if (response.equals("-1")) break;
			Pizza pizza = buildPizza(order.getOrderID());
			pizzas.add(pizza);
		}

		for (Pizza pizza : pizzas) {
			order.addPizza(pizza);
		}

		System.out.println("Do you want to add discounts to this order? Enter y/n?");
		if (reader.readLine().equalsIgnoreCase("y")) {
			while (true) {
				System.out.println("Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				int discountID = Integer.parseInt(reader.readLine());
				if (discountID == -1) break;
				Discount discount = DBNinja.findDiscountByName(String.valueOf(discountID));
				if (discount != null) {
					order.addDiscount(discount);
				}
			}
		}

		DBNinja.addOrder(order);
		System.out.println("Finished adding order...Returning to menu...");
	}
	
	
	public static void viewCustomers() throws SQLException, IOException 
	{
		/*
		 * Simply print out all of the customers from the database. 
		 */


		ArrayList<Customer> customers = DBNinja.getCustomerList();
		for (Customer c : customers) {
			System.out.println(c);
		}
	}
	

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException 
	{
		/*
		 * Ask for the name of the customer:
		 *   First Name <space> Last Name
		 * 
		 * Ask for the  phone number.
		 *   (##########) (No dash/space)
		 * 
		 * Once you get the name and phone number, add it to the DB
		 */
		
		// User Input Prompts...
		System.out.println("What is this customer's name (first <space> last)");
		String[] name = reader.readLine().split(" ");
		System.out.println("What is this customer's phone number (##########) (No dash/space)");
		String phone = reader.readLine();
		Customer customer = new Customer(0, name[0], name[1], phone);
		DBNinja.addCustomer(customer);

	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException 
	{
		/*  
		* This method allows the user to select between three different views of the Order history:
		* The program must display:
		* a.	all open orders
		* b.	all completed orders 
		* c.	all the orders (open and completed) since a specific date (inclusive)
		* 
		* After displaying the list of orders (in a condensed format) must allow the user to select a specific order for viewing its details.  
		* The details include the full order type information, the pizza information (including pizza discounts), and the order discounts.
		* 
		*/ 
			
		
		// User Input Prompts...
		System.out.println("Would you like to:\n(a) display all orders [open or closed]\n(b) display all open orders\n(c) display all completed [closed] orders\n(d) display orders since a specific date");
		String choice = reader.readLine();

		ArrayList<Order> orders = new ArrayList<>();
		switch (choice) {
			case "a":
				orders = DBNinja.getOrders(false);
				break;
			case "b":
				orders = DBNinja.getOrders(true);
				break;
			case "c":
				orders = DBNinja.getOrders(false);
				orders.removeIf(o -> o.getIsComplete() != 1);
				break;
			case "d":
				System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
				String date = reader.readLine();
				orders = DBNinja.getOrdersByDate(date);
				break;
			default:
				System.out.println("I don't understand that input, returning to menu");
				return;
		}

		if (orders.isEmpty()) {
			System.out.println("No orders to display, returning to menu.");
			return;
		}

		for (Order order : orders) {
			System.out.println(order.toSimplePrint());
		}

		System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
		int orderID = Integer.parseInt(reader.readLine());
		if (orderID == -1) return;
		Order order = orders.stream().filter(o -> o.getOrderID() == orderID).findFirst().orElse(null);
		if (order == null) {
			System.out.println("Incorrect entry, returning to menu.");
			return;
		}

		System.out.println("Order Details:");
		System.out.println(order);
	}

	
	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException 
	{
		/*
		 * All orders that are created through java (part 3, not the orders from part 2) should start as incomplete
		 * 
		 * When this method is called, you should print all of the "opoen" orders marked
		 * and allow the user to choose which of the incomplete orders they wish to mark as complete
		 * 
		 */
		
		
		
		// User Input Prompts...
		ArrayList<Order> orders = DBNinja.getOrders(true);
		if (orders.isEmpty()) {
			System.out.println("There are no open orders currently... returning to menu...");
			return;
		}

		for (Order order : orders) {
			System.out.println(order.toSimplePrint());
		}

		System.out.println("Which order would you like mark as complete? Enter the OrderID: ");
		int orderID = Integer.parseInt(reader.readLine());
		Order order = orders.stream().filter(o -> o.getOrderID() == orderID).findFirst().orElse(null);
		if (order == null) {
			System.out.println("Incorrect entry, not an option");
			return;
		}

		DBNinja.completeOrder(order);

	}

	public static void ViewInventoryLevels() throws SQLException, IOException 
	{
		/*
		 * Print the inventory. Display the topping ID, name, and current inventory
		*/


		ArrayList<Topping> toppings = DBNinja.getToppingList();
		for (Topping t : toppings) {
			System.out.println(t);
		}
	}


	public static void AddInventory() throws SQLException, IOException 
	{
		/*
		 * This should print the current inventory and then ask the user which topping (by ID) they want to add more to and how much to add
		 */
		
		
		// User Input Prompts...
		ViewInventoryLevels();
		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		int toppingID = Integer.parseInt(reader.readLine());
		Topping topping = DBNinja.findToppingByName(String.valueOf(toppingID));
		if (topping == null) {
			System.out.println("Incorrect entry, not an option");
			return;
		}

		System.out.println("How many units would you like to add? ");
		double quantity = Double.parseDouble(reader.readLine());
		DBNinja.addToInventory(topping, quantity);
	}

	// A method that builds a pizza. Used in our add new order method
	public static Pizza buildPizza(int orderID) throws SQLException, IOException 
	{
		
		/*
		 * This is a helper method for first menu option.
		 * 
		 * It should ask which size pizza the user wants and the crustType.
		 * 
		 * Once the pizza is created, it should be added to the DB.
		 * 
		 * We also need to add toppings to the pizza. (Which means we not only need to add toppings here, but also our bridge table)
		 * 
		 * We then need to add pizza discounts (again, to here and to the database)
		 * 
		 * Once the discounts are added, we can return the pizza
		 */

		System.out.println("What size is the pizza?");
		System.out.println("1. " + DBNinja.size_s);
		System.out.println("2. " + DBNinja.size_m);
		System.out.println("3. " + DBNinja.size_l);
		System.out.println("4. " + DBNinja.size_xl);
		System.out.println("Enter the corresponding number: ");
		String sizeChoice = reader.readLine();

		System.out.println("What crust for this pizza?");
		System.out.println("1. " + DBNinja.crust_thin);
		System.out.println("2. " + DBNinja.crust_orig);
		System.out.println("3. " + DBNinja.crust_pan);
		System.out.println("4. " + DBNinja.crust_gf);
		System.out.println("Enter the corresponding number: ");
		String crustChoice = reader.readLine();

		Pizza pizza = new Pizza(0, sizeChoice, crustChoice, orderID, "in progress", "", 0.0, 0.0);
		DBNinja.addPizza(pizza);

		while (true) {
			System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			int toppingID = Integer.parseInt(reader.readLine());
			if (toppingID == -1) break;

			Topping topping = DBNinja.findToppingByName(String.valueOf(toppingID));
			if (topping == null) {
				System.out.println("Incorrect entry, not an option");
				continue;
			}

			System.out.println("Do you want to add extra topping? Enter y/n");
			boolean isDoubled = reader.readLine().equalsIgnoreCase("y");

			DBNinja.useTopping(pizza, topping, isDoubled);
		}

		System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
		if (reader.readLine().equalsIgnoreCase("y")) {
			while (true) {
				System.out.println("Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				int discountID = Integer.parseInt(reader.readLine());
				if (discountID == -1) break;

				Discount discount = DBNinja.findDiscountByName(String.valueOf(discountID));
				if (discount == null) {
					System.out.println("Incorrect entry, not an option");
					continue;
				}

				DBNinja.usePizzaDiscount(pizza, discount);
			}
		}

		return pizza;
	}
	
	
	public static void PrintReports() throws SQLException, NumberFormatException, IOException
	{
		/*
		 * This method asks the use which report they want to see and calls the DBNinja method to print the appropriate report.
		 * 
		 */

		System.out.println("Which report do you wish to print? Enter\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType:");
		String choice = reader.readLine();

		switch (choice) {
			case "a":
				DBNinja.printToppingPopReport();
				break;
			case "b":
				DBNinja.printProfitByPizzaReport();
				break;
			case "c":
				DBNinja.printProfitByOrderType();
				break;
			default:
				System.out.println("I don't understand that input... returning to menu...");
		}
	}

	//Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "6f1b7ea9aac470402d48f7916ea6a010";

	
	private static void autograder_compilation_check() {

		try {
			Order o = null;
			Pizza p = null;
			Topping t = null;
			Discount d = null;
			Customer c = null;
			ArrayList<Order> alo = null;
			ArrayList<Discount> ald = null;
			ArrayList<Customer> alc = null;
			ArrayList<Topping> alt = null;
			double v = 0.0;
			String s = "";

			DBNinja.addOrder(o);
			DBNinja.addPizza(p);
			DBNinja.useTopping(p, t, false);
			DBNinja.usePizzaDiscount(p, d);
			DBNinja.useOrderDiscount(o, d);
			DBNinja.addCustomer(c);
			DBNinja.completeOrder(o);
			alo = DBNinja.getOrders(false);
			o = DBNinja.getLastOrder();
			alo = DBNinja.getOrdersByDate("01/01/1999");
			ald = DBNinja.getDiscountList();
			d = DBNinja.findDiscountByName("Discount");
			alc = DBNinja.getCustomerList();
			c = DBNinja.findCustomerByPhone("0000000000");
			alt = DBNinja.getToppingList();
			t = DBNinja.findToppingByName("Topping");
			DBNinja.addToInventory(t, 1000.0);
			v = DBNinja.getBaseCustPrice("size", "crust");
			v = DBNinja.getBaseBusPrice("size", "crust");
			DBNinja.printInventory();
			DBNinja.printToppingPopReport();
			DBNinja.printProfitByPizzaReport();
			DBNinja.printProfitByOrderType();
			s = DBNinja.getCustomerName(0);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}


}


