package com.unicasolid.shophub.service;

import com.unicasolid.shophub.exception.OrderValidationException;
import com.unicasolid.shophub.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ATTENTION: Classe initiale "God Class" du MVP (Sprint 0).
 * Elle effectue la validation, le calcul de remise, le calcul de taxes, le paiement,
 * la persistance et la notification courriel dans une seule et même classe !
 * Cible du refactoring du Sprint 1 (SRP).
 */
@Service
public class OrderCheckoutService {

    private final Map<String, Order> orderDatabase = new ConcurrentHashMap<>();

    public Order checkout(Customer customer, List<OrderItem> items, String couponCode) {
        // 1. Validation du panier
        validateCart(customer, items);

        // 2. Calcul du sous-total
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getTotalPrice());
        }

        // 3. Calcul des remises (Code promo / VIP)
        BigDecimal discount = calculateDiscounts(subtotal, customer, items, couponCode);

        // 4. Calcul des taxes (TVA française)
        BigDecimal tax = calculateTaxes(items);

        // Montant final = (Sous-total - Remise) + Taxes
        BigDecimal taxableSubtotal = subtotal.subtract(discount);
        if (taxableSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            taxableSubtotal = BigDecimal.ZERO;
        }
        BigDecimal finalAmount = taxableSubtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        // 5. Création de la commande
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(
                orderId,
                customer,
                new ArrayList<>(items),
                subtotal.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP),
                tax.setScale(2, RoundingMode.HALF_UP),
                finalAmount,
                OrderStatus.PENDING,
                LocalDateTime.now()
        );

        // 6. Traitement du paiement (simulation Stripe en direct)
        processPayment(order);
        order.setStatus(OrderStatus.PAID);

        // 7. Persistance en base de données
        saveOrder(order);

        // 8. Notification client (simulation SMTP en direct)
        sendConfirmationEmail(order);

        return order;
    }

    private void validateCart(Customer customer, List<OrderItem> items) {
        if (customer == null) {
            throw new OrderValidationException("Le client est obligatoire.");
        }
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("Le panier ne peut pas être vide.");
        }
        for (OrderItem item : items) {
            if (item.quantity() <= 0) {
                throw new OrderValidationException("La quantité de l'article doit être strictement positive.");
            }
            if (item.quantity() > 20) {
                throw new OrderValidationException("Quantité maximale de 20 par article dépassée: " + item.productName());
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new OrderValidationException("Le prix de l'article doit être positif.");
            }
        }
    }

    private BigDecimal calculateDiscounts(BigDecimal subtotal, Customer customer, List<OrderItem> items, String couponCode) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // Règle 1: Client VIP (> 100€ => 10% de remise globale)
        if (customer.type() == CustomerType.VIP && subtotal.compareTo(new BigDecimal("100.00")) >= 0) {
            BigDecimal vipDiscount = subtotal.multiply(new BigDecimal("0.10"));
            totalDiscount = totalDiscount.add(vipDiscount);
        }

        // Règle 2: Code BLACKFRIDAY (20% sur les produits ELECTRONICS)
        if ("BLACKFRIDAY".equalsIgnoreCase(couponCode)) {
            for (OrderItem item : items) {
                if (item.category() == ProductCategory.ELECTRONICS) {
                    BigDecimal electDiscount = item.getTotalPrice().multiply(new BigDecimal("0.20"));
                    totalDiscount = totalDiscount.add(electDiscount);
                }
            }
        }

        // Règle 3: Code WELCOME10 (10€ de remise immédiate pour panier > 30€)
        if ("WELCOME10".equalsIgnoreCase(couponCode) && subtotal.compareTo(new BigDecimal("30.00")) >= 0) {
            totalDiscount = totalDiscount.add(new BigDecimal("10.00"));
        }

        return totalDiscount;
    }

    private BigDecimal calculateTaxes(List<OrderItem> items) {
        BigDecimal totalTax = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal rate;
            if (item.category() == ProductCategory.FOOD || item.category() == ProductCategory.BOOKS) {
                rate = new BigDecimal("0.055"); // TVA réduite 5.5%
            } else {
                rate = new BigDecimal("0.20"); // TVA standard 20.0%
            }
            BigDecimal itemTax = item.getTotalPrice().multiply(rate);
            totalTax = totalTax.add(itemTax);
        }
        return totalTax;
    }

    private void processPayment(Order order) {
        // Appel bancaire en dur
        System.out.println("[PAIEMENT] Débit de " + order.getFinalAmount() + "€ pour la commande " + order.getId());
    }

    private void saveOrder(Order order) {
        // Enregistrement en dur dans le stockage interne
        orderDatabase.put(order.getId(), order);
        System.out.println("[BDD] Commande sauvegardée avec l'identifiant: " + order.getId());
    }

    private void sendConfirmationEmail(Order order) {
        // Envoi d'email en dur
        System.out.println("[EMAIL] Confirmation envoyée à " + order.getCustomer().email() +
                " pour un montant total de " + order.getFinalAmount() + "€");
    }

    public Optional<Order> findOrderById(String orderId) {
        return Optional.ofNullable(orderDatabase.get(orderId));
    }
}
