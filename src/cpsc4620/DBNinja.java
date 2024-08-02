package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";

	public final static int PARAM_INDEX_ONE = 1;
	public final static int PARAM_INDEX_TWO = 2;
	public final static int PARAM_INDEX_THREE = 3;
	public final static int PARAM_INDEX_FOUR = 4;
	public final static int PARAM_INDEX_FIVE = 5;
	public final static int PRAM_INDEX_SIX = 6;


	
	private static boolean connect_to_db() throws SQLException, IOException {

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			// Test print
			System.out.println("Could not connect to Database.");
			return false;
		} catch (IOException e) {
			// Test print
			System.out.println("Could not connect to Database.");
			return false;
		}

	}

	
	public static void addOrder(Order o) throws SQLException, IOException 
	{
		connect_to_db();
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, and pickup tables
		 * 
		 */

		String query = "INSERT INTO customer_order (OrderCustomerID, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		pstmt.setInt(PARAM_INDEX_ONE, o.getCustID());
		pstmt.setString(PARAM_INDEX_TWO, o.getOrderType());
		pstmt.setDouble(PARAM_INDEX_THREE, o.getCustPrice());
		pstmt.setDouble(PARAM_INDEX_FOUR, o.getBusPrice());
		pstmt.setString(PARAM_INDEX_FIVE, o.getOrderType());
		pstmt.executeUpdate();

		// Let the database handle giving the Order an ID
		ResultSet keys = pstmt.getGeneratedKeys();
		if (keys.next()) {
			o.setOrderID(keys.getInt(PARAM_INDEX_ONE));
		}

		// Let the database handle giving the Order a timestamp
		String timestampQuery = "SELECT OrderTimestamp FROM customer_order WHERE OrderID = ?";
		PreparedStatement tsPstmt = conn.prepareStatement(timestampQuery);
		tsPstmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
		ResultSet tsRs = tsPstmt.executeQuery();
		if (tsRs.next()) {
			o.setDate(tsRs.getString(PARAM_INDEX_ONE));
		}

		// Add to the corresponding subtype table
		switch (o.getOrderType()) {
			case dine_in:
				String dineInQuery = "INSERT INTO dine_in (DineInOrderID, TableNumber) VALUES (?, ?)";
				PreparedStatement dineInStmt = conn.prepareStatement(dineInQuery);
				dineInStmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
				dineInStmt.setInt(PARAM_INDEX_TWO, ((DineinOrder) o).getTableNum());
				dineInStmt.executeUpdate();
				break;
			case pickup:
				String pickupQuery = "INSERT INTO pickup (PickupOrderID, PickupTimestamp, PickupCustomerID) VALUES (?, ?, ?)";
				PreparedStatement pickupStmt = conn.prepareStatement(pickupQuery);
				pickupStmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
				pickupStmt.setTimestamp(PARAM_INDEX_TWO, Timestamp.valueOf(o.getDate()));
				pickupStmt.setInt(PARAM_INDEX_THREE, o.getCustID());
				pickupStmt.executeUpdate();
				break;
			case delivery:
				String deliveryQuery = "INSERT INTO delivery (DeliveryOrderID, DeliveryAddress, DeliveryCustomerID) VALUES (?, ?, ?)";
				PreparedStatement deliveryStmt = conn.prepareStatement(deliveryQuery);
				deliveryStmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
				deliveryStmt.setString(PARAM_INDEX_TWO, ((DeliveryOrder) o).getAddress());
				deliveryStmt.setInt(PARAM_INDEX_THREE, o.getCustID());
				deliveryStmt.executeUpdate();
				break;
		}

		// Add pizzas to the order
		for (Pizza p : o.getPizzaList()) {
			addPizza(p);
		}

		// Apply order discounts
		for (Discount d : o.getDiscountList()) {
			useOrderDiscount(o, d);
		}

		conn.close();
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void addPizza(Pizza p) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind adding pizza discounts and toppings associated with the pizza,
		 * there are other methods below that may help with that process.
		 * 
		 */

		String pizzaQuery = "INSERT INTO pizza (PizzaSizeType, PizzaCrustType, PizzaState, OrderID, PizzaTotalPrice, PizzaTotalCost) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pizzaStmt = conn.prepareStatement(pizzaQuery, Statement.RETURN_GENERATED_KEYS);
		pizzaStmt.setString(PARAM_INDEX_ONE, p.getSize());
		pizzaStmt.setString(PARAM_INDEX_TWO, p.getCrustType());
		pizzaStmt.setString(PARAM_INDEX_THREE, p.getPizzaState());
		pizzaStmt.setInt(PARAM_INDEX_FOUR, p.getOrderID());
		pizzaStmt.setDouble(PARAM_INDEX_FIVE, p.getCustPrice());
		pizzaStmt.setDouble(PRAM_INDEX_SIX, p.getBusPrice());

		// Let the database handle giving the Pizza an ID
		pizzaStmt.executeUpdate();
		ResultSet keys = pizzaStmt.getGeneratedKeys();
		if (keys.next()) {
			p.setPizzaID(keys.getInt(PARAM_INDEX_ONE));
		}

		// Let the database handle giving the Pizza a date (timestamp)
		String timestampQuery = "SELECT PizzaTimestamp FROM pizza WHERE PizzaID = ?";
		PreparedStatement tsPstmt = conn.prepareStatement(timestampQuery);
		tsPstmt.setInt(PARAM_INDEX_ONE, p.getPizzaID());
		ResultSet tsRs = tsPstmt.executeQuery();
		if (tsRs.next()) {
			p.setPizzaDate(tsRs.getString(PARAM_INDEX_ONE));
		}

		// Add toppings to the pizza
		boolean[] isDoubledArray = p.getIsDoubleArray();
		ArrayList<Topping> toppings = p.getToppings();
		for (int i = 0; i < toppings.size(); i++) {
			Topping t = toppings.get(i);
			boolean isDoubled = isDoubledArray[i];
			useTopping(p, t, isDoubled);
		}

		// Apply pizza discounts
		for (Discount d : p.getDiscounts()) {
			usePizzaDiscount(p, d);
		}

		conn.close();
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	
	public static void useTopping(Pizza p, Topping t, boolean isDoubled) throws SQLException, IOException //this method will update toppings inventory in SQL and add entities to the Pizzatops table. Pass in the p pizza that is using t topping
	{
		connect_to_db();
		/*
		 * This method should do 2 two things.
		 * - update the topping inventory every time we use t topping (accounting for extra toppings as well)
		 * - connect the topping to the pizza
		 *   What that means will be specific to your yimplementatinon.
		 * 
		 * Ideally, you should't let toppings go negative....but this should be dealt with BEFORE calling this method.
		 * 
		 */

		double quantityUsed = 0.0;
		switch (p.getSize()) {
			case DBNinja.size_s:
				quantityUsed = t.getPerAMT();
				break;
			case DBNinja.size_m:
				quantityUsed = t.getMedAMT();
				break;
			case DBNinja.size_l:
				quantityUsed = t.getLgAMT();
				break;
			case DBNinja.size_xl:
				quantityUsed = t.getXLAMT();
				break;
		}

		if (isDoubled) {
			quantityUsed *= PARAM_INDEX_TWO;
		}

		// Update topping inventory
		String updateToppingQuery = "UPDATE topping SET ToppingCurrLvl = ToppingCurrLvl - ? WHERE ToppingID = ?";
		PreparedStatement updateToppingStmt = conn.prepareStatement(updateToppingQuery);
		updateToppingStmt.setDouble(PARAM_INDEX_ONE, quantityUsed);
		updateToppingStmt.setInt(PARAM_INDEX_TWO, t.getTopID());
		updateToppingStmt.executeUpdate();

		// Connect the topping to the pizza
		String pizzaToppingQuery = "INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (?, ?)";
		PreparedStatement pizzaToppingStmt = conn.prepareStatement(pizzaToppingQuery);
		pizzaToppingStmt.setInt(PARAM_INDEX_ONE, p.getPizzaID());
		pizzaToppingStmt.setInt(PARAM_INDEX_TWO, t.getTopID());
		pizzaToppingStmt.executeUpdate();

		conn.close();
		
		
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	
	public static void usePizzaDiscount(Pizza p, Discount d) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * This method connects a discount with a Pizza in the database.
		 * 
		 * What that means will be specific to your implementatinon.
		 */

		String query = "INSERT INTO pizza_discount (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, p.getPizzaID());
		pstmt.setInt(PARAM_INDEX_TWO, d.getDiscountID());
		pstmt.executeUpdate();

		conn.close();
		
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * This method connects a discount with an order in the database
		 * 
		 * You might use this, you might not depending on where / how to want to update
		 * this information in the dabast
		 */


		String query = "INSERT INTO order_discount (OrderDiscountOrderID, OrderDiscountDiscountID) VALUES (?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
		pstmt.setInt(PARAM_INDEX_TWO, d.getDiscountID());
		pstmt.executeUpdate();

		conn.close();
		
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method adds a new customer to the database.
		 * 
		 */

		String query = "INSERT INTO customer (CustomerFirstName, CustomerLastName, CustomerPhone) VALUES (?, ?, ?)";
		PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(PARAM_INDEX_ONE, c.getFName());
		pstmt.setString(PARAM_INDEX_TWO, c.getLName());
		pstmt.setString(PARAM_INDEX_THREE, c.getPhone());
		pstmt.executeUpdate();

		ResultSet keys = pstmt.getGeneratedKeys();

		if (keys.next()) {
			c.setCustID(keys.getInt(PARAM_INDEX_ONE));
		}

		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void completeOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Find the specifed order in the database and mark that order as complete in the database.
		 * 
		 */


		String query = "UPDATE customer_order SET OrderIsComplete = 1 WHERE OrderID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, o.getOrderID());
		pstmt.executeUpdate();

		// Mark the order as complete in the Order object
		o.setIsComplete(PARAM_INDEX_ONE);
		conn.close();
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}


	public static ArrayList<Order> getOrders(boolean openOnly) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Return an arraylist of all of the orders.
		 * 	openOnly == true => only return a list of open (ie orders that have not been marked as completed)
		 *           == false => return a list of all the orders in the database
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */

		ArrayList<Order> orders = new ArrayList<>();
		String query;

		if (openOnly) {
			// Sorted by orderID just like in the video
			query = "SELECT * FROM customer_order WHERE OrderIsComplete = 0 ORDER BY OrderID";
		} else {
			query = "SELECT * FROM customer_order ORDER BY OrderID";
		}

		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			int orderID = rs.getInt("OrderID");
			int custID = rs.getInt("OrderCustomerID");
			String orderType = rs.getString("OrderType");
			String date = rs.getTimestamp("OrderTimestamp").toString();
			double custPrice = rs.getDouble("OrderTotalPrice");
			double busPrice = rs.getDouble("OrderTotalCost");
			int isComplete = rs.getInt("OrderIsComplete");

			Order order;
			switch (orderType) {
				case dine_in:
					String dineInQuery = "SELECT * FROM dine_in WHERE DineInOrderID = ?";
					PreparedStatement dineInStmt = conn.prepareStatement(dineInQuery);
					dineInStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet dineInRs = dineInStmt.executeQuery();
					dineInRs.next();
					int tableNumber = dineInRs.getInt("TableNumber");
					order = new DineinOrder(orderID, custID, date, custPrice, busPrice, isComplete, tableNumber);
					break;
				case pickup:
					String pickupQuery = "SELECT * FROM pickup WHERE PickupOrderID = ?";
					PreparedStatement pickupStmt = conn.prepareStatement(pickupQuery);
					pickupStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet pickupRs = pickupStmt.executeQuery();
					pickupRs.next();
					int isPickedUp = pickupRs.getInt("isPickedUp");
					order = new PickupOrder(orderID, custID, date, custPrice, busPrice, isPickedUp, isComplete);
					break;
				case delivery:
					String deliveryQuery = "SELECT * FROM delivery WHERE DeliveryOrderID = ?";
					PreparedStatement deliveryStmt = conn.prepareStatement(deliveryQuery);
					deliveryStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet deliveryRs = deliveryStmt.executeQuery();
					deliveryRs.next();
					String address = deliveryRs.getString("DeliveryAddress");
					order = new DeliveryOrder(orderID, custID, date, custPrice, busPrice, isComplete, address);
					break;
				default:
					throw new SQLException("Unknown order type: " + orderType);
			}

			orders.add(order);
		}

		conn.close();
		return orders;

		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION

	}
	
	public static Order getLastOrder() throws SQLException, IOException{
		/*
		 * Query the database for the LAST order added
		 * then return an Order object for that order.
		 * NOTE...there should ALWAYS be a "last order"!
		 */
		connect_to_db();
		Order order = null;

		// Decided to ORDER BY OrderID because it is more reliable than the timestamp
		// and by doing it this way I can use it in EnterOrder() to determine the newest
		// orderID
		String query = "SELECT * FROM customer_order ORDER BY OrderID DESC LIMIT 1";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			int orderID = rs.getInt("OrderID");
			int custID = rs.getInt("OrderCustomerID");
			String orderType = rs.getString("OrderType");
			String date = rs.getTimestamp("OrderTimestamp").toString();
			double custPrice = rs.getDouble("OrderTotalPrice");
			double busPrice = rs.getDouble("OrderTotalCost");
			int isComplete = rs.getInt("OrderisComplete");

			switch (orderType) {
				case dine_in:
					String dineInQuery = "SELECT * FROM dine_in WHERE DineInOrderOrderID = ?";
					PreparedStatement dineInStmt = conn.prepareStatement(dineInQuery);
					dineInStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet dineInRs = dineInStmt.executeQuery();
					dineInRs.next();
					int tableNumber = dineInRs.getInt("TableNumber");
					order = new DineinOrder(orderID, custID, date, custPrice, busPrice, isComplete, tableNumber);
					break;
				case pickup:
					String pickupQuery = "SELECT * FROM pickup WHERE PickupOrderOrderID = ?";
					PreparedStatement pickupStmt = conn.prepareStatement(pickupQuery);
					pickupStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet pickupRs = pickupStmt.executeQuery();
					pickupRs.next();
					int isPickedUp = pickupRs.getInt("isPickedUp");
					order = new PickupOrder(orderID, custID, date, custPrice, busPrice, isPickedUp, isComplete);
					break;
				case delivery:
					String deliveryQuery = "SELECT * FROM delivery WHERE DeliveryOrderOrderID = ?";
					PreparedStatement deliveryStmt = conn.prepareStatement(deliveryQuery);
					deliveryStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet deliveryRs = deliveryStmt.executeQuery();
					deliveryRs.next();
					String address = deliveryRs.getString("DeliveryAddress");
					order = new DeliveryOrder(orderID, custID, date, custPrice, busPrice, isComplete, address);
					break;
				default:
					throw new SQLException("Unknown order type: " + orderType);
			}
		}

		conn.close();
		return order;

	}

	public static ArrayList<Order> getOrdersByDate(String date) throws SQLException, IOException{
		/*
		 * Query the database for ALL the orders placed on a specific date
		 * and return a list of those orders.
		 *  
		 */

		// Above it says to query the database for all order placed ON a specific date
		// The prompt however says display order SINCE a specific date & the video on canvas does as well
		// So because the odds that you get results are higher when you filter for SINCE a specific date
		// I will go with that.
		connect_to_db();

		ArrayList<Order> orders = new ArrayList<>();
		String query = "SELECT * FROM customer_order WHERE DATE(OrderTimestamp) >= ? ORDER BY OrderTimestamp";

		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, date);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			int orderID = rs.getInt("OrderID");
			int custID = rs.getInt("OrderCustomerID");
			String orderType = rs.getString("OrderType");
			String dateTime = rs.getTimestamp("OrderTimestamp").toString();
			double custPrice = rs.getDouble("OrderTotalPrice");
			double busPrice = rs.getDouble("OrderTotalCost");
			int isComplete = rs.getInt("OrderIsComplete");

			Order order;
			switch (orderType) {
				case dine_in:
					String dineInQuery = "SELECT * FROM dine_in WHERE DineInOrderID = ?";
					PreparedStatement dineInStmt = conn.prepareStatement(dineInQuery);
					dineInStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet dineInRs = dineInStmt.executeQuery();
					dineInRs.next();
					int tableNumber = dineInRs.getInt("TableNumber");
					order = new DineinOrder(orderID, custID, dateTime, custPrice, busPrice, isComplete, tableNumber);
					break;
				case pickup:
					String pickupQuery = "SELECT * FROM pickup WHERE PickupOrderID = ?";
					PreparedStatement pickupStmt = conn.prepareStatement(pickupQuery);
					pickupStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet pickupRs = pickupStmt.executeQuery();
					pickupRs.next();
					int isPickedUp = pickupRs.getInt("isPickedUp");
					order = new PickupOrder(orderID, custID, dateTime, custPrice, busPrice, isPickedUp, isComplete);
					break;
				case delivery:
					String deliveryQuery = "SELECT * FROM delivery WHERE DeliveryOrderID = ?";
					PreparedStatement deliveryStmt = conn.prepareStatement(deliveryQuery);
					deliveryStmt.setInt(PARAM_INDEX_ONE, orderID);
					ResultSet deliveryRs = deliveryStmt.executeQuery();
					deliveryRs.next();
					String address = deliveryRs.getString("DeliveryAddress");
					order = new DeliveryOrder(orderID, custID, dateTime, custPrice, busPrice, isComplete, address);
					break;
				default:
					throw new SQLException("Unknown order type: " + orderType);
			}

			orders.add(order);
		}

		conn.close();
		return orders;
	}
		
	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database for all the available discounts and 
		 * return them in an arrayList of discounts.
		 * 
		*/

		ArrayList<Discount> discounts = new ArrayList<>();
		// Grabbing the discounts and ordering them by ID since that is cleaner
		String query = "SELECT * FROM discount ORDER BY DiscountID";

		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount = rs.getDouble("DiscountAmount");
			boolean isPercent = rs.getBoolean("DiscountType");

			Discount discount = new Discount(discountID, discountName, amount, isPercent);
			discounts.add(discount);
		}

		conn.close();
		return discounts;
	}

	public static Discount findDiscountByName(String name) throws SQLException, IOException{
		/*
		 * Query the database for a discount using it's name.
		 * If found, then return an OrderDiscount object for the discount.
		 * If it's not found....then return null
		 *  
		 */
		connect_to_db();

		String query = "SELECT * FROM discount WHERE DiscountName = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, name);
		ResultSet rs = pstmt.executeQuery();

		Discount discount = null;

		if (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount;
			boolean isPercent;

			if (rs.getObject("PercentDiscount") != null) {
				amount = rs.getDouble("PercentDiscount");
				isPercent = true;
			} else {
				amount = rs.getDouble("DollarDiscount");
				isPercent = false;
			}

			discount = new Discount(discountID, discountName, amount, isPercent);
		}

		conn.close();
		return discount;
	}


	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the data for all the customers and return an arrayList of all the customers. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		*/

		ArrayList<Customer> customers = new ArrayList<>();
		String query = "SELECT * FROM customer ORDER BY CustomerLastName, CustomerFirstName, CustomerPhone";

		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			int custID = rs.getInt("CustomerID");
			String fName = rs.getString("CustomerFirstName");
			String lName = rs.getString("CustomerLastName");
			String phone = rs.getString("CustomerPhone");

			Customer customer = new Customer(custID, fName, lName, phone);
			customers.add(customer);
		}

		conn.close();
		return customers;
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static Customer findCustomerByPhone(String phoneNumber)throws SQLException, IOException{
		/*
		 * Query the database for a customer using a phone number.
		 * If found, then return a Customer object for the customer.
		 * If it's not found....then return null
		 *  
		 */
		connect_to_db();

		String query = "SELECT * FROM customer WHERE CustomerPhone = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, phoneNumber);
		ResultSet rs = pstmt.executeQuery();

		Customer customer = null;

		if (rs.next()) {
			int custID = rs.getInt("CustomerID");
			String fName = rs.getString("CustomerFirstName");
			String lName = rs.getString("CustomerLastName");
			String phone = rs.getString("CustomerPhone");

			customer = new Customer(custID, fName, lName, phone);
		}

		conn.close();
		return customer;
	}


	public static ArrayList<Topping> getToppingList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for the aviable toppings and 
		 * return an arrayList of all the available toppings. 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */
		ArrayList<Topping> toppings = new ArrayList<>();
		// Decided to display the toppings by orderding them by OrderID like in the video example. (looks cleaner)
		String query = "SELECT * FROM topping ORDER BY ToppingID";

		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			int toppingID = rs.getInt("ToppingID");
			String toppingType = rs.getString("ToppingType");
			double toppingPrice = rs.getDouble("ToppingPrice");
			double toppingCost = rs.getDouble("ToppingCost");
			double toppingAmountSM = rs.getDouble("ToppingAmountSM");
			double toppingAmountMD = rs.getDouble("ToppingAmountMD");
			double toppingAmountLG = rs.getDouble("ToppingAmountLG");
			double toppingAmountXL = rs.getDouble("ToppingAmountXL");
			int toppingMinLvl = rs.getInt("ToppingMinLvl");
			int toppingCurrLvl = rs.getInt("ToppingCurrLvl");

			Topping topping = new Topping(toppingID, toppingType, toppingAmountSM, toppingAmountMD, toppingAmountLG, toppingAmountXL, toppingPrice, toppingCost, toppingMinLvl, toppingCurrLvl);
			toppings.add(topping);
		}

		conn.close();
		return toppings;
	}

	public static Topping findToppingByName(String name) throws SQLException, IOException{
		connect_to_db();
		/*
		 * Query the database for the topping using it's name.
		 * If found, then return a Topping object for the topping.
		 * If it's not found....then return null
		 *  
		 */
		String query = "SELECT * FROM topping WHERE ToppingType = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, name);
		ResultSet rs = pstmt.executeQuery();

		Topping topping = null;

		if (rs.next()) {
			int toppingID = rs.getInt("ToppingID");
			String toppingType = rs.getString("ToppingType");
			double toppingPrice = rs.getDouble("ToppingPrice");
			double toppingCost = rs.getDouble("ToppingCost");
			double toppingAmountSM = rs.getDouble("ToppingAmountSM");
			double toppingAmountMD = rs.getDouble("ToppingAmountMD");
			double toppingAmountLG = rs.getDouble("ToppingAmountLG");
			double toppingAmountXL = rs.getDouble("ToppingAmountXL");
			int toppingMinLvl = rs.getInt("ToppingMinLvl");
			int toppingCurrLvl = rs.getInt("ToppingCurrLvl");

			topping = new Topping(toppingID, toppingType, toppingAmountSM, toppingAmountMD, toppingAmountLG, toppingAmountXL, toppingPrice, toppingCost, toppingMinLvl, toppingCurrLvl);
		}

		conn.close();
		return topping;

	}


	public static void addToInventory(Topping t, double quantity) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Updates the quantity of the topping in the database by the amount specified.
		 * 
		 * */
		String query = "UPDATE topping SET ToppingCurrLvl = ToppingCurrLvl + ? WHERE ToppingID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setDouble(PARAM_INDEX_ONE, quantity);
		pstmt.setInt(PARAM_INDEX_TWO, t.getTopID());
		pstmt.executeUpdate();

		conn.close();
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database fro the base customer price for that size and crust pizza.
		 * 
		*/
		String query = "SELECT BasePrice FROM base_price_cost WHERE SizeType = ? AND CrustType = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, size);
		pstmt.setString(PARAM_INDEX_TWO, crust);
		ResultSet rs = pstmt.executeQuery();

		double basePrice = 0.0;

		if (rs.next()) {
			basePrice = rs.getDouble("BasePrice");
		}

		conn.close();
		return basePrice;
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/* 
		 * Query the database fro the base business price for that size and crust pizza.
		 * 
		*/

		String query = "SELECT BaseCost FROM base_price_cost WHERE SizeType = ? AND CrustType = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(PARAM_INDEX_ONE, size);
		pstmt.setString(PARAM_INDEX_TWO, crust);
		ResultSet rs = pstmt.executeQuery();

		double baseCost = 0.0;

		if (rs.next()) {
			baseCost = rs.getDouble("BaseCost");
		}

		conn.close();
		return baseCost;
	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Queries the database and prints the current topping list with quantities.
		 *  
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT ToppingType, ToppingCurrLvl FROM topping ORDER BY ToppingType";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		System.out.println("Current Topping Inventory:");
		System.out.println("----------------------------");
		System.out.println("Topping Type      | Quantity");
		System.out.println("----------------------------");

		while (rs.next()) {
			String toppingType = rs.getString("ToppingType");
			int quantity = rs.getInt("ToppingCurrLvl");
			System.out.printf("%-18s | %d%n", toppingType, quantity);
		}

		conn.close();
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void printToppingPopReport() throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT * FROM ToppingPopularity ORDER BY ToppingCount DESC";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		System.out.println("Topping Popularity Report:");
		System.out.println("----------------------------");
		System.out.println("Topping         | Count");
		System.out.println("----------------------------");

		while (rs.next()) {
			String topping = rs.getString("Topping");
			int count = rs.getInt("ToppingCount");
			System.out.printf("%-15s | %d%n", topping, count);
		}

		conn.close();
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void printProfitByPizzaReport() throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT * FROM ProfitByPizza ORDER BY Profit DESC";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		System.out.println("Profit By Pizza Report:");
		System.out.println("-------------------------------------------------");
		System.out.println("Size       | Crust        | Profit   | Order Month");
		System.out.println("-------------------------------------------------");

		while (rs.next()) {
			String size = rs.getString("Size");
			String crust = rs.getString("Crust");
			double profit = rs.getDouble("Profit");
			String orderMonth = rs.getString("OrderMonth");
			System.out.printf("%-10s | %-12s | %-8.2f | %s%n", size, crust, profit, orderMonth);
		}

		conn.close();
		
		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}
	
	public static void printProfitByOrderType() throws SQLException, IOException
	{
		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT * FROM ProfitByOrderType ORDER BY customerType, Profit DESC";
		PreparedStatement pstmt = conn.prepareStatement(query);
		ResultSet rs = pstmt.executeQuery();

		System.out.println("Profit By Order Type Report:");
		System.out.println("-----------------------------------------------------------");
		System.out.println("Customer Type  | Order Month | Total Order Price | Total Order Cost | Profit");
		System.out.println("-----------------------------------------------------------");

		while (rs.next()) {
			String customerType = rs.getString("customerType");
			String orderMonth = rs.getString("OrderMonth");
			double totalOrderPrice = rs.getDouble("TotalOrderPrice");
			double totalOrderCost = rs.getDouble("TotalOrderCost");
			double profit = rs.getDouble("Profit");
			System.out.printf("%-15s | %-11s | %-17.2f | %-15.2f | %.2f%n", customerType, orderMonth, totalOrderPrice, totalOrderCost, profit);
		}

		conn.close();
		//DO NOT FORGET TO CLOSE YOUR CONNECTION	
	}
	
	
	
	public static String getCustomerName(int CustID) throws SQLException, IOException
	{
	/*
		 * This is a helper method to fetch and format the name of a customer
		 * based on a customer ID. This is an example of how to interact with 
		 * your database from Java.  It's used in the model solution for this project...so the code works!
		 * 
		 * OF COURSE....this code would only work in your application if the table & field names match!
		 *
		 * Correct so I modified this helper function to make my table and fields
		 */

		 connect_to_db();

		/* 
		 * an example query using a constructed string...
		 * remember, this style of query construction could be subject to sql injection attacks!
		 * 
		 */
		String cname1 = "";
		String query = "SELECT CustomerFirstName, CustomerLastName FROM customer WHERE CustomerID=" + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);
		
		while(rset.next())
		{
			cname1 = rset.getString(PARAM_INDEX_ONE) + " " + rset.getString(PARAM_INDEX_TWO);
		}

		/* 
		* an example of the same query using a prepared statement...
		* 
		*/
		String cname2 = "";
		PreparedStatement os;
		ResultSet rset2;
		String query2;
		query2 = "SELECT CustomerFirstName, CustomerLastName FROM customer WHERE CustomerID=?;";
		os = conn.prepareStatement(query2);
		os.setInt(1, CustID);
		rset2 = os.executeQuery();
		while(rset2.next())
		{
			cname2 = rset2.getString("CustomerFirstName") + " " + rset2.getString("CustomerLastName"); // note the use of field names in the getSting methods
		}

		conn.close();
		return cname1; // OR cname2
	}

	/*
	 * The next 3 private methods help get the individual components of a SQL datetime object. 
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0,4));
	}
	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}
	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder)
	{
		if(getYear(dateOfOrder) > year)
			return true;
		else if(getYear(dateOfOrder) < year)
			return false;
		else
		{
			if(getMonth(dateOfOrder) > month)
				return true;
			else if(getMonth(dateOfOrder) < month)
				return false;
			else
			{
				if(getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
		}
	}

	// Helper function to get customer by their ID for the Existing customer prompt
	public static Customer getCustomerById(int customerID) throws SQLException, IOException {
		connect_to_db();
		Customer customer = null;

		String query = "SELECT * FROM customer WHERE CustomerID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, customerID);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			String firstName = rs.getString("CustomerFirstName");
			String lastName = rs.getString("CustomerLastName");
			String phone = rs.getString("CustomerPhone");
			customer = new Customer(customerID, firstName, lastName, phone);
		}

		conn.close();
		return customer;
	}

	// Helper function to simply get topping by ID
	public static Topping getToppingById(int toppingID) throws SQLException, IOException {
		connect_to_db();
		Topping topping = null;

		String query = "SELECT * FROM topping WHERE ToppingID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, toppingID);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			String topName = rs.getString("ToppingType");
			double perAMT = rs.getDouble("ToppingAmountSM");
			double medAMT = rs.getDouble("ToppingAmountMD");
			double lgAMT = rs.getDouble("ToppingAmountLG");
			double xLAMT = rs.getDouble("ToppingAmountXL");
			double custPrice = rs.getDouble("ToppingPrice");
			double bustPrice = rs.getDouble("ToppingCost");
			int MinINVT = rs.getInt("ToppingMinLvl");
			int curINVT = rs.getInt("ToppingCurrLvl");
			topping = new Topping(toppingID, topName, perAMT, medAMT, lgAMT,xLAMT, custPrice, bustPrice, MinINVT, curINVT);
		}

		conn.close();
		return topping;
	}

	// Helper function to simply get the discount by ID
	public static Discount getDiscountbyId(int discountID) throws SQLException, IOException {
		connect_to_db();
		Discount discount = null;

		String query = "SELECT * FROM discount WHERE DiscountID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(PARAM_INDEX_ONE, discountID);
		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			String discountName = rs.getString("DiscountName");
			double discountAmount = rs.getDouble("DiscountAmount");
			boolean discountType = rs.getBoolean("DiscountType");

			discount = new Discount(discountID, discountName, discountAmount, discountType);
		}

		conn.close();
		return discount;
	}

	// This helper function is used to get the details about an order
	// However it is funky sometimes and for some order is can return
	// something but for others "connection closed" and wasn't sure how to fix that
	// so I at least wrapped it in a try catch
	public static Order getOrderById(int orderID) throws SQLException, IOException {
		connect_to_db();

		Order order = null;
		Pizza pizza = null;

		try {
			String query = "SELECT * FROM customer_order WHERE OrderID = ?";
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(PARAM_INDEX_ONE, orderID);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int custID = rs.getInt("OrderCustomerID");
				String orderType = rs.getString("OrderType");
				String date = rs.getString("OrderTimestamp");
				double custPrice = rs.getDouble("OrderTotalPrice");
				double busPrice = rs.getDouble("OrderTotalCost");
				int isComplete = rs.getInt("OrderIsComplete");

				order = new Order(orderID, custID, orderType, date, custPrice, busPrice, isComplete);
			}
			rs.close();
			pstmt.close();

			if (order != null) {
				String query2 = "SELECT * FROM pizza WHERE OrderID = ?";
				PreparedStatement pstmt2 = conn.prepareStatement(query2);
				pstmt2.setInt(PARAM_INDEX_ONE, orderID);
				ResultSet rs2 = pstmt2.executeQuery();

				ArrayList<Pizza> pizzas = new ArrayList<>();

				while (rs2.next()) {
					int pizzaID = rs2.getInt("PizzaID");
					String size = rs2.getString("PizzaSizeType");
					String crustType = rs2.getString("PizzaCrustType");
					String pizzaState = rs2.getString("PizzaState");
					String pizzaDate = rs2.getString("PizzaTimestamp");
					double pizzaCustPrice = rs2.getDouble("PizzaTotalPrice");
					double pizzaBusPrice = rs2.getDouble("PizzaTotalCost");

					pizza = new Pizza(pizzaID, size, crustType, orderID, pizzaState, pizzaDate, pizzaCustPrice, pizzaBusPrice);
					pizzas.add(pizza);
					order.addPizza(pizza);
				}
				rs2.close();
				pstmt2.close();

				String pizzaDiscountQuery = "SELECT * FROM pizza_discount WHERE PizzaDiscountPizzaID = ?";
				PreparedStatement pstmt3 = conn.prepareStatement(pizzaDiscountQuery);
				for (Pizza p : pizzas) {
					pstmt3.setInt(PARAM_INDEX_ONE, p.getPizzaID());
					ResultSet rs3 = pstmt3.executeQuery();

					while (rs3.next()) {
						int pizzaDiscountDiscountID = rs3.getInt("PizzaDiscountDiscountID");
						Discount discount = getDiscountbyId(pizzaDiscountDiscountID);
						p.addDiscounts(discount);
					}
					rs3.close();
				}
				pstmt3.close();

			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error occurred while retrieving order: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return order;
	}

	// Helper function to update the Order pricing after pizza/discount has been added
	public static void updateOrder(int orderID, double updatedPrice, double updatedCost) throws SQLException, IOException {
		connect_to_db();

		String query = "UPDATE customer_order SET OrderTotalPrice = ?, OrderTotalCost = ? WHERE OrderID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setDouble(PARAM_INDEX_ONE, updatedPrice);
		pstmt.setDouble(PARAM_INDEX_TWO, updatedCost);
		pstmt.setInt(PARAM_INDEX_THREE, orderID);

		pstmt.executeUpdate();

		conn.close();
	}

	// Helper function to update the pizza pricing after topping/discount has been added
	public static void updatePizza(int pizzaID, double updatedPrice, double updatedCost) throws SQLException, IOException {
		connect_to_db();

		String query = "UPDATE pizza SET PizzaTotalPrice = ?, PizzaTotalCost = ? WHERE PizzaID = ?";
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setDouble(PARAM_INDEX_ONE, updatedPrice);
		pstmt.setDouble(PARAM_INDEX_TWO, updatedCost);
		pstmt.setInt(PARAM_INDEX_THREE, pizzaID);

		pstmt.executeUpdate();

		conn.close();
	}
}