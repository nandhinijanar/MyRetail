CREATE KEYSPACE myRetailKeySpace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3};

CREATE TABLE myRetailKeySpace.product_price(productID int, price decimal, currencyCode text, primary key(productID));

INSERT INTO myRetailKeySpace.product_price(productID, price, currencyCode) VALUES (13860428, 13.49, 'USD');

INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (15117729, 12.00, 'USD');
INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16483589, 52.78, 'USD');
INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16696652, 79.00, 'USD');
INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16752456, 278.33, 'USD');

SELECT * FROM myRetailKeySpace.product_price;