-- CreateTables.sql
-- Michael Steiger

CREATE schema Pizzeria;
USE Pizzeria;

-- Create customer table
CREATE TABLE customer (
    CustomerID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerFirstName VARCHAR(50) NOT NULL,
    CustomerLastName VARCHAR(50) NOT NULL,
    CustomerPhone VARCHAR(15) NOT NULL
);

-- Create base_price_cost table
CREATE TABLE base_price_cost (
    SizeType VARCHAR(50) NOT NULL,
    CrustType VARCHAR(50) NOT NULL,
    BaseCost DECIMAL(10, 2) NOT NULL,
    BasePrice DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (SizeType, CrustType),
    INDEX (SizeType),
    INDEX (CrustType)
);

-- Create customer_order table
CREATE TABLE customer_order (
    OrderID INT AUTO_INCREMENT PRIMARY KEY,
    OrderCustomerID INT NOT NULL,
    OrderTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    OrderType VARCHAR(50) NOT NULL,
    OrderTotalPrice DECIMAL(10, 2) NOT NULL,
    OrderTotalCost DECIMAL(10, 2) NOT NULL,
    OrderSubType VARCHAR(50) NOT NULL,
    OrderIsComplete INT DEFAULT 0 NOT NULL,
    FOREIGN KEY (OrderCustomerID) REFERENCES customer(CustomerID)
);

-- Create pizza table
CREATE TABLE pizza (
    PizzaID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaSizeType VARCHAR(50) NOT NULL,
    PizzaCrustType VARCHAR(50) NOT NULL,
    OrderID INT NOT NULL,
    PizzaState VARCHAR(50) DEFAULT "in progress",
    PizzaTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PizzaTotalPrice DECIMAL(10 , 2 ),
    PizzaTotalCost DECIMAL(10 , 2 ),
    FOREIGN KEY (PizzaCrustType , PizzaSizeType)
        REFERENCES base_price_cost (CrustType , SizeType),
    FOREIGN KEY (OrderID)
        REFERENCES customer_order (OrderID)
);

-- Create topping table
CREATE TABLE topping (
    ToppingID INT AUTO_INCREMENT PRIMARY KEY,
    ToppingType VARCHAR(50) NOT NULL,
    ToppingPrice DECIMAL(10, 2) NOT NULL,
    ToppingCost DECIMAL(10, 2) NOT NULL,
    ToppingAmountSM DECIMAL(5, 2),
    ToppingAmountMD DECIMAL(5, 2),
    ToppingAmountLG DECIMAL(5, 2),
    ToppingAmountXL DECIMAL(5, 2),
    ToppingMinLvl INT,
    ToppingCurrLvl INT
);

-- Create pizza_topping table
CREATE TABLE pizza_topping (
    PizzaToppingPizzaID INT NOT NULL,
    PizzaToppingToppingID INT NOT NULL,
    PizzaToppingIsDoubled INT DEFAULT 0 NOT NULL,
    PRIMARY KEY (PizzaToppingPizzaID, PizzaToppingToppingID),
    FOREIGN KEY (PizzaToppingPizzaID) REFERENCES pizza(PizzaID),
    FOREIGN KEY (PizzaToppingToppingID) REFERENCES topping(ToppingID)
);

-- Create discount table
CREATE TABLE discount (
    DiscountID INT AUTO_INCREMENT PRIMARY KEY,
    DiscountName VARCHAR(50) NOT NULL,
    DiscountAmount DECIMAL(5, 2),
    DiscountType Boolean NOT NULL
);

-- Create pizza_discount table
CREATE TABLE pizza_discount (
    PizzaDiscountID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaDiscountPizzaID INT NOT NULL,
    PizzaDiscountDiscountID INT NOT NULL,
    FOREIGN KEY (PizzaDiscountPizzaID) REFERENCES pizza(PizzaID),
    FOREIGN KEY (PizzaDiscountDiscountID) REFERENCES discount(DiscountID)
);

-- Create order_discount table
CREATE TABLE order_discount (
    OrderDiscountID INT AUTO_INCREMENT PRIMARY KEY,
    OrderDiscountOrderID INT NOT NULL,
    OrderDiscountDiscountID INT NOT NULL,
    FOREIGN KEY (OrderDiscountOrderID) REFERENCES customer_order(OrderID),
    FOREIGN KEY (OrderDiscountDiscountID) REFERENCES discount(DiscountID)
);

-- Create dine_in table
CREATE TABLE dine_in (
    DineInOrderID INT NOT NULL,
    TableNumber INT NOT NULL,
    PRIMARY KEY (DineInOrderID),
    FOREIGN KEY (DineInOrderID) REFERENCES customer_order(OrderID)
);

-- Create delivery table
CREATE TABLE delivery (
    DeliveryOrderID INT NOT NULL,
    DeliveryAddress VARCHAR(255) NOT NULL,
    DeliveryCustomerID INT NOT NULL,
    PRIMARY KEY (DeliveryOrderID),
    FOREIGN KEY (DeliveryOrderID) REFERENCES customer_order(OrderID),
    FOREIGN KEY (DeliveryCustomerID) REFERENCES customer(CustomerID)
);

-- Create pickup table
CREATE TABLE pickup (
    PickupOrderID INT NOT NULL,
    PickupTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PickupCustomerID INT NOT NULL,
    isPickedUp INT DEFAULT 0 NOT NULL,
    PRIMARY KEY (PickupOrderID),
    FOREIGN KEY (PickupOrderID) REFERENCES customer_order(OrderID),
    FOREIGN KEY (PickupCustomerID) REFERENCES customer(CustomerID)
);
