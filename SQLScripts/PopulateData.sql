USE Pizzeria;

-- Populate the topping table
INSERT INTO topping (ToppingType, ToppingPrice, ToppingCost, ToppingAmountSM, ToppingAmountMD, ToppingAmountLG, ToppingAmountXL, ToppingMinLvl, ToppingCurrLvl) VALUES
('Pepperoni', 1.25, 0.2, 2, 2.75, 3.5, 4.5, 50, 100),
('Sausage', 1.25, 0.15, 2.5, 3, 3.5, 4.25, 50, 100),
('Ham', 1.5, 0.15, 2, 2.5, 3.25, 4, 25, 78),
('Chicken', 1.75, 0.25, 1.5, 2, 2.25, 3, 25, 56),
('Green Pepper', 0.5, 0.02, 1, 1.5, 2, 2.5, 25, 79),
('Onion', 0.5, 0.02, 1, 1.5, 2, 2.75, 25, 85),
('Roma Tomato', 0.75, 0.03, 2, 3, 3.5, 4.5, 10, 86),
('Mushrooms', 0.75, 0.1, 1.5, 2, 2.5, 3, 50, 52),
('Black Olives', 0.6, 0.1, 0.75, 1, 1.5, 2, 25, 39),
('Pineapple', 1, 0.25, 1, 1.25, 1.75, 2, 0, 15),
('Jalapenos', 0.5, 0.05, 0.5, 0.75, 1.25, 1.75, 0, 64),
('Banana Peppers', 0.5, 0.05, 0.6, 1, 1.3, 1.75, 0, 36),
('Regular Cheese', 0.5, 0.12, 2, 3.5, 5, 7, 50, 250),
('Four Cheese Blend', 1, 0.15, 2, 3.5, 5, 7, 25, 150),
('Feta Cheese', 1.5, 0.18, 1.75, 3, 4, 5.5, 0, 75),
('Goat Cheese', 1.5, 0.2, 1.6, 2.75, 4, 5.5, 0, 54),
('Bacon', 1.5, 0.25, 1, 1.5, 2, 3, 0, 89);

-- Populate the discount table (DiscountType is more like: is it a percentage?)
INSERT INTO discount (DiscountName, DiscountAmount, DiscountType) VALUES
('Employee', 15, true),
('Lunch Special Medium', 1.00, false),
('Lunch Special Large', 2.00, false),
('Specialty Pizza', 1.50, false),
('Happy Hour', 10, true),
('Gameday Special', 20, true);

-- Populate the base_price_cost table
INSERT INTO base_price_cost (SizeType, CrustType, BasePrice, BaseCost) VALUES
('Small', 'Thin', 3, 0.5),
('Small', 'Original', 3, 0.75),
('Small', 'Pan', 3.5, 1),
('Small', 'Gluten-Free', 4, 2),
('Medium', 'Thin', 5, 1),
('Medium', 'Original', 5, 1.5),
('Medium', 'Pan', 6, 2.25),
('Medium', 'Gluten-Free', 6.25, 3),
('Large', 'Thin', 8, 1.25),
('Large', 'Original', 8, 2),
('Large', 'Pan', 9, 3),
('Large', 'Gluten-Free', 9.5, 4),
('XLarge', 'Thin', 10, 2),
('XLarge', 'Original', 10, 3),
('XLarge', 'Pan', 11.5, 4.5),
('XLarge', 'Gluten-Free', 12.5, 6);

-- Populate the customer table from the orders
INSERT INTO customer (CustomerFirstName, CustomerLastName, CustomerPhone) VALUES
('Andrew', 'Wilkes-Krier', '864-254-5861'),
('Matt', 'Engers', '864-474-9953'),
('Frank', 'Turner', '864-232-8944'),
('Milo', 'Auckerman', '864-878-5679');

-- Populate the order table and related tables

-- Order 1
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(1, 1, '2024-03-05 12:03:00', 'dinein', 19.75, 3.68, 'dinein');
INSERT INTO dine_in (DineInOrderID, TableNumber) VALUES (1, 21);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (1, 'Thin', 'Large', 19.75, 3.68, 1);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (1, 13), (1, 1), (1, 2);
INSERT INTO pizza_discount (PizzaDiscountID, PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (1, 1, 2);

-- Order 2
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(2, 1, '2024-04-03 12:05:00', 'dinein', 19.78, 4.63, 'dinein');
INSERT INTO dine_in (DineInOrderID, TableNumber) VALUES (2, 4);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (2, 'Pan', 'Medium', 12.85, 3.23, 2);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (2, 15), (2, 9), (2, 7), (2, 8), (2, 12);
INSERT INTO pizza_discount (PizzaDiscountID, PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (2, 2, 1), (3, 2, 4);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (3, 'Original', 'Small', 6.93, 1.40, 2);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (3, 13), (3, 4), (3, 12);

-- Order 3
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(3, 1, '2024-03-03 21:30:00', 'pickup', 89.28, 19.80, 'pickup');
INSERT INTO pickup (PickupOrderID, PickupTimestamp, PickupCustomerID) VALUES (3, '2023-03-03 21:30:00', 1);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (4, 'Original', 'Large', 14.88, 3.30, 3);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (4, 13), (4, 1);

-- Order 4
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(4, 1, '2024-04-20 19:11:00', 'delivery', 86.19, 23.62, 'delivery');
INSERT INTO delivery (DeliveryOrderID, DeliveryAddress, DeliveryCustomerID) VALUES (4, '115 Party Blvd, Anderson SC 29621', 1);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (5, 'Original', 'XLarge', 27.94, 9.19, 4);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (5, 1), (5, 2), (5, 14);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (6, 'Original', 'XLarge', 31.50, 6.25, 4);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (6, 3), (6, 10);
INSERT INTO pizza_discount (PizzaDiscountID, PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (4, 6, 4);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (7, 'Original', 'XLarge', 26.75, 8.18, 4);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (7, 4), (7, 17);
INSERT INTO order_discount (OrderDiscountID, OrderDiscountOrderID, OrderDiscountDiscountID) VALUES (1, 4, 6);

-- Order 5
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(5, 2, '2024-03-02 17:30:00', 'pickup', 27.45, 7.88, 'pickup');
INSERT INTO pickup (PickupOrderID, PickupTimestamp, PickupCustomerID) VALUES (5, '2023-03-02 17:30:00', 2);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (8, 'Gluten-Free', 'XLarge', 27.45, 7.88, 5);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (8, 5), (8, 6), (8, 7), (8, 8), (8, 9), (8, 16);
INSERT INTO pizza_discount (PizzaDiscountID, PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (5, 8, 4);

-- Order 6
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(6, 3, '2024-03-02 18:17:00', 'delivery', 25.81, 4.24, 'delivery');
INSERT INTO delivery (DeliveryOrderID, DeliveryAddress, DeliveryCustomerID) VALUES (6, '6745 Wessex St Anderson SC 29621', 3);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (9, 'Thin', 'Large', 25.81, 4.24, 6);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (9, 4), (9, 5), (9, 6), (9, 8), (9, 14);

-- Order 7
INSERT INTO customer_order (OrderID, OrderCustomerID, OrderTimestamp, OrderType, OrderTotalPrice, OrderTotalCost, OrderSubType) VALUES
(7, 4, '2024-04-13 20:32:00', 'delivery', 37.25, 6.00, 'delivery');
INSERT INTO delivery (DeliveryOrderID, DeliveryAddress, DeliveryCustomerID) VALUES (7, '8879 Suburban Home, Anderson, SC 29621', 4);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (10, 'Thin', 'Large', 18.00, 2.75, 7);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (10, 14);
INSERT INTO pizza (PizzaID, PizzaCrustType, PizzaSizeType, PizzaTotalPrice, PizzaTotalCost, OrderID) VALUES (11, 'Thin', 'Large', 19.25, 3.25, 7);
INSERT INTO pizza_topping (PizzaToppingPizzaID, PizzaToppingToppingID) VALUES (11, 13), (11, 1);
INSERT INTO order_discount (OrderDiscountID, OrderDiscountOrderID, OrderDiscountDiscountID) VALUES (2, 7, 1);
