package org.example;

import java.util.*;

interface shippable {
    String getname();
    double getweight();
}

abstract class product {
    String name;
    Double price;
    int quantity;

    public product(String name, Double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    abstract boolean isexpired();

    boolean isavilable(int requestedquantity) {
        return quantity >= requestedquantity && !isexpired();
    }

    void reduceQuantity(int qty) {
        this.quantity -= qty;
    }

    public String getname() {
        return name;
    }

    public double getprice() {
        return price;
    }
}

abstract class expireprod extends product {
    Date expiredate;

    public expireprod(String name, Double price, int quantity, Date expiredate) {
        super(name, price, quantity);
        this.expiredate = expiredate;
    }

    boolean isexpired() {
        return new Date().after(expiredate);
    }
}

class shippableprod extends product implements shippable {
    double weight;

    public shippableprod(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    boolean isexpired() {
        return false;
    }

    @Override
    public double getweight() {
        return weight;
    }
}

class expireandshipableprod extends expireprod implements shippable {
    double weight;

    public expireandshipableprod(String name, double price, int quantity, double weight, Date expiredate) {
        super(name, price, quantity, expiredate);
        this.weight = weight;
    }

    @Override
    public double getweight() {
        return weight;
    }

    @Override
    public String getname() {
        return name;
    }
}

class noenpireandnoshipableprod extends product {
    public noenpireandnoshipableprod(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    boolean isexpired() {
        return false;
    }
}

class cartitem {
    product prod;
    int quantity;

    public cartitem(product prod, int quantity) {
        this.prod = prod;
        this.quantity = quantity;
    }

    double totalprice() {
        return prod.getprice() * quantity;
    }
}

class customer {
    String name;
    double balance;

    public customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    void deduct(double amount) {
        balance -= amount;
    }

    double getBalance() {
        return balance;
    }
}

class Cart {
    List<cartitem> items = new ArrayList<>();

    void add(product prod, int quantity) {
        if (!prod.isavilable(quantity)) {
            System.out.println("Product unavailable");
        }
        items.add(new cartitem(prod, quantity));
    }

    List<cartitem> getItems() {
        return items;
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    void clear() {
        items.clear();
    }
}

class Shippingservice {
    public static void ship(List<shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, Double> weightMap = new HashMap<>();

        for (shippable item : items) {
            countMap.put(item.getname(), countMap.getOrDefault(item.getname(), 0) + 1);
            weightMap.put(item.getname(), item.getweight());
            totalWeight += item.getweight();
        }

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            String name = entry.getKey();
            int cnt = entry.getValue();
            double w = weightMap.get(name);
            int totalItemWeight = (int) (w * cnt);
            System.out.printf("%dx %-10s %4dg%n", cnt, name, totalItemWeight);
        }

        System.out.printf("Total package weight %.1fkg%n", totalWeight / 1000);
    }
}

class CheckoutSystem {
    public static void checkout(customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
        }

        double subtotal = 0;
        double shippingfee = 0;
        List<shippable> toShip = new ArrayList<>();

        for (cartitem item : cart.getItems()) {
            if (!item.prod.isavilable(item.quantity)) {
                System.out.println("Product out of stock or expired");
            }
            subtotal += item.totalprice();
            if (item.prod instanceof shippable) {
                for (int i = 0; i < item.quantity; i++) {
                    toShip.add((shippable) item.prod);
                }
            }
        }

        if (!toShip.isEmpty()) {
            shippingfee = 30;
            Shippingservice.ship(toShip);
        }

        double total = subtotal + shippingfee;

        if (customer.getBalance() < total) {
            System.out.println("Insufficient balance!");
        }

        customer.deduct(total);
        for (cartitem item : cart.getItems()) {
            item.prod.reduceQuantity(item.quantity);
        }

        System.out.println("** Checkout receipt **");
        for (cartitem item : cart.getItems()) {
            System.out.printf("%dx %-10s %7d%n", item.quantity, item.prod.getname(), (int) item.totalprice());
        }

        System.out.println("----------------------");
        System.out.println("Subtotal         " + (int) subtotal);
        System.out.println("Shipping         " + (int) shippingfee);
        System.out.println("Amount           " + (int) total);

        cart.clear();
    }
}

public class Main {
    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        product cheese = new expireandshipableprod("Cheese", 100, 10, 200, tomorrow);
        product biscuits = new expireandshipableprod("Biscuits", 150, 5, 700, tomorrow);
        product tv = new shippableprod("TV", 5000, 3, 5000);
        product scratchCard = new noenpireandnoshipableprod("Scratch Card", 50, 100);

        customer custm = new customer("Mohamed", 1000);
        Cart cart = new Cart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);

        CheckoutSystem.checkout(custm, cart);
    }
}
