package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.CardDto;
import com.example.e_commerce.dto.PaymentRequest;
import com.example.e_commerce.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Refund;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl {

    @Value("${stripe.secret.key}")
    private String secretKey;

    // Create a Stripe customer for a user
    public String createStripeCustomer(User user) throws StripeException {
        Stripe.apiKey = secretKey;
        
        CustomerCreateParams params = CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(user.getFirstName() + " " + user.getLastName())
            .build();
            
        Customer customer = Customer.create(params);
        return customer.getId();
    }
    
    // Add a payment method (card) to a customer
    public String addCardToCustomer(String stripeCustomerId, CardDto cardDto) throws StripeException {
        Stripe.apiKey = secretKey;
        
        Map<String, Object> card = new HashMap<>();
        card.put("number", cardDto.getCardNumber());
        card.put("exp_month", cardDto.getExpirationMonth());
        card.put("exp_year", cardDto.getExpirationYear());
        card.put("cvc", cardDto.getCvc());
        
        Map<String, Object> params = new HashMap<>();
        params.put("type", "card");
        params.put("card", card);
        
        PaymentMethod paymentMethod = PaymentMethod.create(params);
        
        // Attach payment method to customer
        PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
            .setCustomer(stripeCustomerId)
            .build();
        
        paymentMethod.attach(attachParams);
        
        return paymentMethod.getId();
    }
    
    // Get all cards for a customer
    public List<Map<String, Object>> getCustomerCards(String stripeCustomerId) throws StripeException {
        Stripe.apiKey = secretKey;
        
        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripeCustomerId);
        params.put("type", "card");
        
        PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
        
        List<Map<String, Object>> cards = new ArrayList<>();
        paymentMethods.getData().forEach(method -> {
            Map<String, Object> card = new HashMap<>();
            card.put("id", method.getId());
            card.put("brand", method.getCard().getBrand());
            card.put("last4", method.getCard().getLast4());
            // Update these field names to be consistent with frontend
            card.put("expMonth", method.getCard().getExpMonth());
            card.put("expYear", method.getCard().getExpYear());
            // Keep the original fields for backwards compatibility
            card.put("exp_month", method.getCard().getExpMonth());
            card.put("exp_year", method.getCard().getExpYear());
            cards.add(card);
        });
        
        return cards;
    }
    
    // Process a payment
    public PaymentIntent processPayment(PaymentRequest paymentRequest) throws StripeException {
        Stripe.apiKey = secretKey;
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(paymentRequest.getAmount().longValue() * 100) // Stripe uses cents
            .setCurrency(paymentRequest.getCurrency().toLowerCase())
            .setCustomer(paymentRequest.getStripeCustomerId())
            .setPaymentMethod(paymentRequest.getPaymentMethodId())
            .setConfirm(true)
            .setDescription("Payment for order")
            .build();
            
        return PaymentIntent.create(params);
    }
    
    // Attach a payment method to a customer
    public String attachPaymentMethodToCustomer(String stripeCustomerId, String paymentMethodId) throws StripeException {
        Stripe.apiKey = secretKey;
        
        // Attach payment method to customer
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
            .setCustomer(stripeCustomerId)
            .build();
        
        paymentMethod.attach(attachParams);
        
        return paymentMethod.getId();
    }
    
    // Refund a payment
    public void refundPayment(String paymentIntentId) throws StripeException {
        if (paymentIntentId == null || paymentIntentId.isEmpty()) {
            throw new IllegalArgumentException("Payment intent ID cannot be null or empty");
        }
        
        Stripe.apiKey = secretKey;
        
        try {
            // Get the payment intent to check if it has a charge
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            // If the payment intent has been charged, create a refund
            if (paymentIntent.getLatestCharge() != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("charge", paymentIntent.getLatestCharge());
                
                Refund refund = Refund.create(params);
                System.out.println("Refund processed successfully: " + refund.getId());
            } else {
                // If there's no charge yet, we can cancel the payment intent
                paymentIntent.cancel();
                System.out.println("Payment intent cancelled: " + paymentIntent.getId());
            }
        } catch (StripeException e) {
            System.err.println("Stripe error during refund: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates a partial refund for a specific payment intent
     * 
     * @param paymentIntentId The Stripe payment intent ID
     * @param amount The amount to refund (must be less than or equal to the original payment amount)
     * @return The refund object
     * @throws StripeException If the refund fails
     */
    public Refund createPartialRefund(String paymentIntentId, BigDecimal amount) throws StripeException {
        Stripe.apiKey = secretKey;
        
        // Convert BigDecimal amount to cents (Stripe uses smallest currency unit)
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
        
        Map<String, Object> params = new HashMap<>();
        params.put("payment_intent", paymentIntentId);
        params.put("amount", amountInCents);
        
        return Refund.create(params);
    }
    
    // Add this method to handle deducting from seller's stripe account
    public void deductFromSellerBalance(String stripeAccountId, BigDecimal amount, String reason) throws StripeException {
        Stripe.apiKey = secretKey;
        
        // In a real-world scenario, you would use Stripe Connect to handle 
        // deductions from connected accounts. For now, we're just logging the action.
        System.out.println("Deducting " + amount + " from seller Stripe account: " + stripeAccountId + 
                          " for reason: " + reason);
        
        // For actual implementation with Stripe Connect, you would use code like:
        // Map<String, Object> params = new HashMap<>();
        // params.put("amount", amount.multiply(new BigDecimal("100")).longValue());
        // params.put("currency", "usd");
        // params.put("description", reason);
        // Transfer.create(params);
    }
}