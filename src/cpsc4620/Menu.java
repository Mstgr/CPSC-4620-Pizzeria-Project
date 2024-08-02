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

	public static final int MAGIC_NUMBER_ONE = 1;
	public static final int MAGIC_NUMBER_TWO = 2;
	public static final int MAGIC_NUMBER_THREE = 3;
	public static final int MAGIC_NUMBER_FOUR = 4;
	public static final int MAGIC_NUMBER_SEVENT = 17;

	// Thank you for the opportunity to submit this project.
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
		int orderOption = 0;

		// User Input Prompts...
		System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		String option = reader.readLine();
		orderOption = Integer.parseInt(option);

		// Error Checking user for order options (Dine-in, Pick-up, Delivery)
		while (orderOption < 0 || orderOption > MAGIC_NUMBER_THREE){
			System.out.println("Invalid Choice. Please enter a valid choice below.");
			System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
			option = reader.readLine();
			orderOption = Integer.parseInt(option);
		}

		// Asking the user if they were an existing customer was not required for DineIn order in the video
		// but the DineInOrder Class requires a customerID so I decided to ask every order type if they are
		// an existing customer
		System.out.println("Is this order for an existing customer? Answer y/n: ");
		String existingCustomer = reader.readLine().toUpperCase();
		// Error checking user for existing customer options
		while (!existingCustomer.equals("Y") && !existingCustomer.equals("N")){
			System.out.println("Invalid Choice. Please enter a valid choice below: ");
			System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
			existingCustomer = reader.readLine().toUpperCase();
		}
		Customer customer;
		if (existingCustomer.equals("Y")) {
			System.out.println("Here's a list of the current customers: ");
			viewCustomers();
			System.out.println("Which customer is this order for? Enter ID Number:");
			String customerID = reader.readLine();
			int custID = Integer.parseInt(customerID);
			customer = DBNinja.getCustomerById(custID);

			while (customer == null){
				System.out.println("ERROR: I don't understand your input for: Is this order an existing customer?\n");
				System.out.println("Please enter a valid ID Number from the list below: ");
				viewCustomers();
				System.out.println("Which customer is this order for? Enter ID Number:");
				customerID = reader.readLine();
				custID = Integer.parseInt(customerID);
				customer = DBNinja.getCustomerById(custID);
				//System.out.println("TEST OUTPUT| Customer details: " + customer.toString());
			}
		}else{
			// If the user is not a existing customer make them a new customer
			EnterCustomer();
			viewCustomers();
			System.out.println("Which customer is this order for? Enter ID Number:");
			String customerID = reader.readLine();
			int custID = Integer.parseInt(customerID);
			customer = DBNinja.getCustomerById(custID);
		}

		// Now that we know who the user is we can finally start determining what they want
		Order order;
		int tableNumber;
		if (orderOption == MAGIC_NUMBER_ONE){
			System.out.println("What is the table number for this order?");
			String tableNum = reader.readLine();
			tableNumber = Integer.parseInt(tableNum);
			// Table number error checking
			while (tableNumber < 0){
				System.out.println("Please enter a table number greater than 0.");
				tableNum = reader.readLine();
				tableNumber = Integer.parseInt(tableNum);
			}
			order = new DineinOrder(0, customer.getCustID(), "", 0, 0, 0, tableNumber);

			// Pickup Order option
		}else if (orderOption == MAGIC_NUMBER_TWO){
			order = new PickupOrder(0, customer.getCustID(), "", 0, 0, 0, 0);
		}else{
			System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
			String houseNumber = reader.readLine();
			System.out.println("What is the Street for this order? (e.g., Smile Street)");
			String streetName = reader.readLine();
			System.out.println("What is the City for this order? (e.g., Greenville)");
			String cityName = reader.readLine();
			System.out.println("What is the State for this order? (e.g., SC)");
			String stateName = reader.readLine();
			System.out.println("What is the Zip Code for this order? (e.g., 20605)");
			String zipCode = reader.readLine();
			String customerAddress = houseNumber + " " + streetName + ", " + cityName + ", " + stateName + " " + zipCode;
			order = new DeliveryOrder(0, customer.getCustID(), "", 0, 0, 0, customerAddress);
		}

		DBNinja.addOrder(order);

		System.out.println("Let's build a pizza!");
		Pizza pizza = buildPizza(order.getOrderID());

		// update order pricing after new pizza has been added
		order.setCustPrice(order.getCustID() + pizza.getCustPrice());
		order.setBusPrice(order.getBusPrice() + pizza.getBusPrice());
		DBNinja.updateOrder(order.getOrderID(), order.getCustPrice(), order.getBusPrice());

		System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
		String stopInput = reader.readLine();
		int stopPizzaInput = Integer.parseInt(stopInput);
		while (stopPizzaInput != -1) {
			Pizza pizza2 = buildPizza(order.getOrderID());
			// update order pricing after new pizza has been added
			order.setCustPrice(order.getCustID() + pizza2.getCustPrice());
			order.setBusPrice(order.getBusPrice() + pizza2.getBusPrice());
			DBNinja.updateOrder(order.getOrderID(), order.getCustPrice(), order.getBusPrice());
			System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			stopInput = reader.readLine();
			stopPizzaInput = Integer.parseInt(stopInput);
		}

		System.out.println("Do you want to add discounts to this order? Enter y/n?");
		String addDiscounts = reader.readLine().toUpperCase();
		// Error checking user for existing customer options
		while (!addDiscounts.equals("Y") && !addDiscounts.equals("N")){
			System.out.println("Invalid Choice. Please enter a valid choice below: ");
			System.out.println("Do you want to add discounts to this order? Enter y/n?");
			addDiscounts = reader.readLine().toUpperCase();
		}
		if (addDiscounts.equals("Y")) {
			Discount discount;
			int discountChoice;
			while (true) {
				// Print the discounts for the user (not making a helper function for this)
				ArrayList<Discount> discounts = DBNinja.getDiscountList();
				for (Discount d : discounts) {
					System.out.println(d);
				}
				System.out.println("Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				discountChoice = Integer.parseInt(reader.readLine());
				if (discountChoice == -1) { break; }
				discount = DBNinja.getDiscountbyId(discountChoice);
				order.addDiscount(discount);
			}
		}

		// Update order after potential discount
		DBNinja.updateOrder(order.getOrderID(), order.getCustPrice(), order.getBusPrice());

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
		// Leave Customer ID as 0 since the DB will handle assigning the correct Customer ID (Auto Increment)
		Customer customer = new Customer(0, name[0], name[1], phone);
		DBNinja.addCustomer(customer);
		// Test output
		//System.out.println("New Customer Added! Customer ID: " + customer.getCustID());
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

		Order order = DBNinja.getOrderById(orderID); // Helper method to fetch order details, including pizzas and discounts

		if (order == null) {
			System.out.println("Incorrect entry, returning to menu.");
			return;
		}

		System.out.println("Order Details:");
		System.out.println(order);

		// Display pizza details + Pizza discounts
		System.out.println("Pizzas in this order:");
		for (Pizza pizza : order.getPizzaList()) {
			System.out.println(pizza);
			System.out.println("Pizza Discount:");
			if (pizza.getDiscounts().isEmpty()){
				System.out.println("No Discounts");
			}else{
				System.out.println("On PizzaID="+ pizza.getPizzaID() + ": " + pizza.getDiscounts());
			}
		}

		// Display Order Discounts
		System.out.println("Order Discounts:");
		if (order.getDiscountList().isEmpty()){
			System.out.println("No Discounts");
		}else{
			System.out.println(order.getDiscountList());
		}
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
		// This line grabs the order
		Order order = DBNinja.getOrderById(orderID);
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

		// Print header
		System.out.println(String.format("%-5s | %-20s | %-15s |", "ID", "Topping", "Curr. Inv. Lvl."));
		System.out.println("------------------------------------------------");

		// Print toppings
		for (Topping t : toppings) {
			System.out.println(String.format("%-5d | %-20s | %-15d |", t.getTopID(), t.getTopName(), t.getCurINVT()));
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
		// User Input Error checking
		while (toppingID < MAGIC_NUMBER_ONE || toppingID > MAGIC_NUMBER_SEVENT){
			System.out.println("Invalid topping choice. Please enter a valid topping below.");
			ViewInventoryLevels();
			System.out.println("Which topping do you want to add inventory to? Enter the number: ");
			toppingID = Integer.parseInt(reader.readLine());
		}
		Topping topping = DBNinja.getToppingById(toppingID);

		System.out.println("How many units would you like to add? ");
		double quantity = Double.parseDouble(reader.readLine());
		while (quantity < 0){
			System.out.println("Invalid Quantity Amount. Please enter a valid quantity.");
			quantity = Double.parseDouble(reader.readLine());
		}
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

		// Prompt user for pizza size
		System.out.println("What size is the pizza?");
		System.out.println("1. " + DBNinja.size_s);
		System.out.println("2. " + DBNinja.size_m);
		System.out.println("3. " + DBNinja.size_l);
		System.out.println("4. " + DBNinja.size_xl);
		System.out.println("Enter the corresponding number: ");
		String sizeChoice = reader.readLine();
		int pizzaSizeChoice = Integer.parseInt(sizeChoice);

		// Input Error Checking
		while (pizzaSizeChoice > MAGIC_NUMBER_FOUR || pizzaSizeChoice < MAGIC_NUMBER_ONE) {
			System.out.println("Invalid pizza size. Please enter a valid size below.");
			System.out.println("What size is the pizza?");
			System.out.println("1. " + DBNinja.size_s);
			System.out.println("2. " + DBNinja.size_m);
			System.out.println("3. " + DBNinja.size_l);
			System.out.println("4. " + DBNinja.size_xl);
			System.out.println("Enter the corresponding number: ");
			sizeChoice = reader.readLine();
			pizzaSizeChoice = Integer.parseInt(sizeChoice);
		}

		switch (pizzaSizeChoice) {
			case MAGIC_NUMBER_ONE:
				sizeChoice = DBNinja.size_s;
				break;
			case MAGIC_NUMBER_TWO:
				sizeChoice = DBNinja.size_m;
				break;
			case MAGIC_NUMBER_THREE:
				sizeChoice = DBNinja.size_l;
				break;
			case MAGIC_NUMBER_FOUR:
				sizeChoice = DBNinja.size_xl;
				break;
		}

		// Prompt user for crust type
		System.out.println("What crust for this pizza?");
		System.out.println("1. " + DBNinja.crust_thin);
		System.out.println("2. " + DBNinja.crust_orig);
		System.out.println("3. " + DBNinja.crust_pan);
		System.out.println("4. " + DBNinja.crust_gf);
		System.out.println("Enter the corresponding number: ");
		String crustChoice = reader.readLine();
		int pizzaCrustChoice = Integer.parseInt(crustChoice);

		// User input error checking
		while (pizzaCrustChoice > MAGIC_NUMBER_FOUR || pizzaCrustChoice < MAGIC_NUMBER_ONE){
			System.out.println("Invalid Crust choice. Please enter a valid choice below.");
			System.out.println("What crust for this pizza?");
			System.out.println("1. " + DBNinja.crust_thin);
			System.out.println("2. " + DBNinja.crust_orig);
			System.out.println("3. " + DBNinja.crust_pan);
			System.out.println("4. " + DBNinja.crust_gf);
			System.out.println("Enter the corresponding number: ");
			crustChoice = reader.readLine();
			pizzaCrustChoice = Integer.parseInt(crustChoice);
		}

		switch (pizzaCrustChoice) {
			case MAGIC_NUMBER_ONE:
				crustChoice = DBNinja.crust_thin;
				break;
			case MAGIC_NUMBER_TWO:
				crustChoice = DBNinja.crust_orig;
				break;
			case MAGIC_NUMBER_THREE:
				crustChoice = DBNinja.crust_pan;
				break;
			case MAGIC_NUMBER_FOUR:
				crustChoice = DBNinja.crust_gf;
				break;
		}

		// Get the base Price and Cost for the pizza
		double baseCusPrice = DBNinja.getBaseCustPrice(sizeChoice, crustChoice);
		double baseBusPrice = DBNinja.getBaseBusPrice(sizeChoice, crustChoice);

		Pizza pizza = new Pizza(0, sizeChoice, crustChoice, orderID, "in progress", "", baseCusPrice, baseBusPrice);
		DBNinja.addPizza(pizza);

		while (true) {
			ViewInventoryLevels();
			System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			int toppingID = Integer.parseInt(reader.readLine());
			if (toppingID == -1) break;

			// Quick Error checking for topping
			while (toppingID < MAGIC_NUMBER_ONE || toppingID > MAGIC_NUMBER_SEVENT){
				System.out.println("Invalid topping choice. Please enter a valid topping below.");
				ViewInventoryLevels();
				System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
				toppingID = Integer.parseInt(reader.readLine());
				if (toppingID == -1) break;
			}

			if (toppingID == -1) break;
			Topping topping = DBNinja.getToppingById(toppingID);

			if (topping == null) {
				System.out.println("Incorrect entry, not an option");
				continue;
			}
			// Add Error Checking Later
			System.out.println("Do you want to add extra topping? Enter y/n");
			String isDoubled = reader.readLine().toUpperCase();
			// Input Error Checking
			while (!isDoubled.equals("Y") && !isDoubled.equals("N")){
				System.out.println("Invalid Choice. Please enter a valid choice below: ");
				System.out.println("Do you want to add extra topping? Enter y/n");
				isDoubled = reader.readLine().toUpperCase();
			}
			boolean toppingDoubled;
			// Is the topping to be doubled?
            toppingDoubled = isDoubled.equals("Y");

			// Calculate if we have enough topping
			String pizzaSize = pizza.getSize();
			double quantityToBeUsed = 0.0;
			switch(pizzaSize){
				case DBNinja.size_s:
					quantityToBeUsed = topping.getPerAMT();
					break;
				case DBNinja.size_m:
					quantityToBeUsed = topping.getMedAMT();
					break;
				case DBNinja.size_l:
					quantityToBeUsed = topping.getLgAMT();
					break;
				case DBNinja.size_xl:
					quantityToBeUsed = topping.getXLAMT();
					break;
			}
			if (toppingDoubled){
				quantityToBeUsed *= MAGIC_NUMBER_TWO;
			}

			// If the Current inventory - the quantity that is about to be used is greater than the
			// Minimum level that that topping can be we use that topping -> so we don't "run" out
			if (topping.getCurINVT() - quantityToBeUsed > topping.getMinINVT()){
				DBNinja.useTopping(pizza, topping, toppingDoubled);
			}else{
				System.out.println(topping.getTopName() + " is currently unavailable. Please select a different topping.");
			}
			pizza.addToppings(topping, toppingDoubled);
			DBNinja.updatePizza(pizza.getPizzaID(), pizza.getCustPrice(), pizza.getBusPrice());
		}




		System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
		String discountPizza = reader.readLine().toUpperCase();

		// Error checking for discount pizza prompt
		while (!discountPizza.equals("Y") && !discountPizza.equals("N")){
			System.out.println("Invalid Choice. Please enter a valid choice below: ");
			System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
			discountPizza = reader.readLine().toUpperCase();
		}

		if (discountPizza.equals("Y")) {
			while (true) {
				// Print the discounts for the user (not making a helper function for this)
				ArrayList<Discount> discounts = DBNinja.getDiscountList();
				for (Discount d : discounts){
					System.out.println(d);
				}

				System.out.println("Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				int discountID = Integer.parseInt(reader.readLine());
				if (discountID == -1) break;


				Discount discount = DBNinja.getDiscountbyId(discountID);
				if (discount == null) {
					System.out.println("Incorrect entry, not an option");
					continue;
				}

				pizza.addDiscounts(discount);
				DBNinja.updatePizza(pizza.getPizzaID(), pizza.getCustPrice(), pizza.getBusPrice());
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


