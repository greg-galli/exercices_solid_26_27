package com.unicasolid.shophub.service;

import com.unicasolid.shophub.exception.OrderValidationException;
import com.unicasolid.shophub.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderCheckoutServiceTest {

    private OrderCheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new OrderCheckoutService();
    }

    @Test
    @DisplayName("Devrait valider et régler une commande standard avec calcul exact de TVA 20%")
    void shouldCheckoutStandardCustomerSuccessfully() {
        Customer customer = new Customer("c1", "Alice Dupont", "alice@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Casque Audio", ProductCategory.ELECTRONICS, new BigDecimal("100.00"), 1)
        );

        Order order = checkoutService.checkout(customer, items, null);

        assertNotNull(order.getId());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getSubtotal());
        assertEquals(new BigDecimal("0.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("20.00"), order.getTaxAmount()); // TVA 20%
        assertEquals(new BigDecimal("120.00"), order.getFinalAmount()); // 100 + 20
        assertTrue(checkoutService.findOrderById(order.getId()).isPresent());
    }

    @Test
    @DisplayName("Devrait appliquer la remise de 10% pour un client VIP sur un panier >= 100€")
    void shouldApplyVipDiscountWhenEligible() {
        Customer vipCustomer = new Customer("c2", "Bob VIP", "bob@example.com", CustomerType.VIP);
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Clavier Mécanique", ProductCategory.ELECTRONICS, new BigDecimal("100.00"), 1)
        );

        Order order = checkoutService.checkout(vipCustomer, items, null);

        assertEquals(new BigDecimal("100.00"), order.getSubtotal());
        assertEquals(new BigDecimal("10.00"), order.getDiscountAmount()); // 10% remise
        assertEquals(new BigDecimal("20.00"), order.getTaxAmount()); // TVA sur produit (20% de 100)
        assertEquals(new BigDecimal("110.00"), order.getFinalAmount()); // (100 - 10) + 20 = 110.00
    }

    @Test
    @DisplayName("Devrait appliquer la remise BLACKFRIDAY de 20% uniquement sur les articles électroniques")
    void shouldApplyBlackFridayCouponOnElectronics() {
        Customer customer = new Customer("c3", "Charlie", "charlie@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Souris Gamer", ProductCategory.ELECTRONICS, new BigDecimal("50.00"), 1),
                new OrderItem("p2", "T-shirt", ProductCategory.CLOTHING, new BigDecimal("30.00"), 1)
        );

        Order order = checkoutService.checkout(customer, items, "BLACKFRIDAY");

        // Sous-total = 80€
        // Remise = 20% sur 50€ = 10€
        // Taxes = 20% de 50€ (10€) + 20% de 30€ (6€) = 16€
        // Final = (80 - 10) + 16 = 86€
        assertEquals(new BigDecimal("80.00"), order.getSubtotal());
        assertEquals(new BigDecimal("10.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("16.00"), order.getTaxAmount());
        assertEquals(new BigDecimal("86.00"), order.getFinalAmount());
    }

    @Test
    @DisplayName("Devrait appliquer le bon WELCOME10 de 10€ pour une commande >= 30€")
    void shouldApplyWelcomeCouponWhenEligible() {
        Customer customer = new Customer("c3b", "David", "david@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p2", "T-shirt", ProductCategory.CLOTHING, new BigDecimal("40.00"), 1)
        );

        Order order = checkoutService.checkout(customer, items, "WELCOME10");

        // Sous-total = 40€
        // Remise = 10€
        // Taxes = 20% de 40€ = 8€
        // Final = (40 - 10) + 8 = 38€
        assertEquals(new BigDecimal("40.00"), order.getSubtotal());
        assertEquals(new BigDecimal("10.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("8.00"), order.getTaxAmount());
        assertEquals(new BigDecimal("38.00"), order.getFinalAmount());
    }

    @Test
    @DisplayName("Devrait appliquer le taux de TVA réduit à 5.5% pour la nourriture et les livres")
    void shouldApplyReducedTaxRateForFoodAndBooks() {
        Customer customer = new Customer("c4", "Diana", "diana@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p3", "Livre Clean Code", ProductCategory.BOOKS, new BigDecimal("40.00"), 1),
                new OrderItem("p4", "Panier Bio", ProductCategory.FOOD, new BigDecimal("60.00"), 1)
        );

        Order order = checkoutService.checkout(customer, items, null);

        // Sous-total = 100€
        // Remise = 0€
        // Taxes = 5.5% de 100€ = 5.50€
        // Final = 105.50€
        assertEquals(new BigDecimal("100.00"), order.getSubtotal());
        assertEquals(new BigDecimal("5.50"), order.getTaxAmount());
        assertEquals(new BigDecimal("105.50"), order.getFinalAmount());
    }

    @Test
    @DisplayName("Devrait lever une exception si le client est null")
    void shouldThrowExceptionWhenCustomerIsNull() {
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Livre", ProductCategory.BOOKS, new BigDecimal("10.00"), 1)
        );
        assertThrows(OrderValidationException.class, () -> checkoutService.checkout(null, items, null));
    }

    @Test
    @DisplayName("Devrait lever une exception si le panier est vide")
    void shouldThrowExceptionWhenCartIsEmpty() {
        Customer customer = new Customer("c5", "Evan", "evan@example.com", CustomerType.STANDARD);
        assertThrows(OrderValidationException.class, () -> checkoutService.checkout(customer, Collections.emptyList(), null));
    }

    @Test
    @DisplayName("Devrait lever une exception si un article dépasse la quantité maximale autorisée de 20")
    void shouldThrowExceptionWhenItemQuantityExceedsLimit() {
        Customer customer = new Customer("c6", "Frank", "frank@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Clé USB", ProductCategory.ELECTRONICS, new BigDecimal("10.00"), 25)
        );
        assertThrows(OrderValidationException.class, () -> checkoutService.checkout(customer, items, null));
    }

    @Test
    @DisplayName("Devrait lever une exception si le prix d'un article est négatif ou nul")
    void shouldThrowExceptionWhenItemPriceIsNegativeOrZero() {
        Customer customer = new Customer("c7", "Grace", "grace@example.com", CustomerType.STANDARD);
        List<OrderItem> items = List.of(
                new OrderItem("p1", "Article Gratuit", ProductCategory.CLOTHING, BigDecimal.ZERO, 1)
        );
        assertThrows(OrderValidationException.class, () -> checkoutService.checkout(customer, items, null));
    }
}
