-- Michael Steiger
-- CreateViews.sql

USE Pizzeria;

-- Create view for ToppingPopularity
CREATE VIEW ToppingPopularity AS
SELECT
    topping.ToppingType AS Topping,
    COUNT(pizza_topping.PizzaToppingToppingID) AS ToppingCount
FROM
    pizza_topping
JOIN
    topping ON pizza_topping.PizzaToppingToppingID = topping.ToppingID
GROUP BY
    topping.ToppingType
ORDER BY
    ToppingCount DESC;

-- View data from ToppingPopularity
SELECT * FROM ToppingPopularity;

-- Create view for ProfitByPizza
CREATE VIEW ProfitByPizza AS
SELECT
    pizza.PizzaSizeType AS Size,
    pizza.PizzaCrustType AS Crust,
    (SUM(pizza.PizzaTotalPrice) - SUM(pizza.PizzaTotalCost)) AS Profit,
    DATE_FORMAT(customer_order.OrderTimestamp, '%m/%Y') AS OrderMonth
FROM
    pizza
JOIN
    customer_order ON pizza.OrderID = customer_order.OrderID
GROUP BY
    pizza.PizzaSizeType, pizza.PizzaCrustType, DATE_FORMAT(customer_order.OrderTimestamp, '%m/%Y')
ORDER BY
    Profit DESC;

-- View data from ProfitByPizza
SELECT * FROM ProfitByPizza;

-- Create view for ProfitByOrderType
CREATE VIEW ProfitByOrderType AS
SELECT
    customer_order.OrderSubType AS customerType,
    DATE_FORMAT(customer_order.OrderTimestamp, '%m/%Y') AS OrderMonth,
    SUM(customer_order.OrderTotalPrice) AS TotalOrderPrice,
    SUM(customer_order.OrderTotalCost) AS TotalOrderCost,
    (SUM(customer_order.OrderTotalPrice) - SUM(customer_order.OrderTotalCost)) AS Profit
FROM
    customer_order
GROUP BY
    customer_order.OrderSubType, DATE_FORMAT(customer_order.OrderTimestamp, '%m/%Y')
WITH ROLLUP;

-- View data from ProfitByOrderType
SELECT * FROM ProfitByOrderType;
