// PaymentServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.CardDto;
import com.example.e_commerce.dto.PaymentRequest;
import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.PaymentRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository payRepo;
    private final UserRepository userRepository;
    private final StripeServiceImpl stripeService;

    @Override
    public Payment savePayment(Payment payment) {
        return payRepo.save(payment);
    }

    @Override
    public List<Payment> getAllPayments() {
        return payRepo.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
        return payRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public void deletePayment(Long id) {
        payRepo.deleteById(id);
    }

    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        return payRepo.findByUserUserId(userId);
    }

    @Transactional
    @Override
    public String createCustomerIfNotExists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isEmpty()) {
            return user.getStripeCustomerId();
        }

        try {
            String stripeCustomerId = stripeService.createStripeCustomer(user);
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
            return stripeCustomerId;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage());
        }
    }

    @Override
    public String addCardToCustomer(Long userId, CardDto cardDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {
            throw new RuntimeException("User does not have a Stripe customer account");
        }

        try {
            // Create the payment method with Stripe
            String paymentMethodId = stripeService.createStripeCustomer(user);
            stripeService.attachPaymentMethodToCustomer(paymentMethodId, user.getStripeCustomerId());
            
            // Save card information to the database
            Payment payment = Payment.builder()
                    .user(user)
                    .paymentMethod("card")
                    .stripeCustomerId(user.getStripeCustomerId())
                    .cardLastFour(cardDto.getCardNumber().substring(cardDto.getCardNumber().length() - 4))
                    .cardExpirationMonth(cardDto.getExpirationMonth())
                    .cardExpirationYear(cardDto.getExpirationYear())
                    .status("active")
                    .build();
            
            payRepo.save(payment);
            
            return paymentMethodId;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to add card: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getUserCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {
            throw new RuntimeException("User does not have a Stripe customer account");
        }

        try {
            return stripeService.getCustomerCards(user.getStripeCustomerId());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to get cards: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Payment processPayment(PaymentRequest paymentRequest) {
        User user = userRepository.findById(paymentRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            PaymentIntent paymentIntent = stripeService.processPayment(paymentRequest);
            
            // Create payment record in database
            Payment payment = Payment.builder()
                    .user(user)
                    .amount(paymentRequest.getAmount())
                    .currency(paymentRequest.getCurrency())
                    .stripeCustomerId(paymentRequest.getStripeCustomerId())
                    .stripePaymentIntentId(paymentIntent.getId())
                    .paymentMethod("card")
                    .status(paymentIntent.getStatus())
                    .build();
            
            // Get card details from payment intent
            if (paymentIntent.getPaymentMethod() != null) {
                Charge charge = Charge.retrieve(paymentIntent.getLatestCharge());
                payment.setCardBrand(charge.getPaymentMethodDetails().getCard().getBrand());
                payment.setCardLastFour(charge.getPaymentMethodDetails().getCard().getLast4());
            }
            
            return payRepo.save(payment);
        } catch (StripeException e) {
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }
    }

    @Override
    public String attachPaymentMethod(Long userId, String paymentMethodId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {
            throw new RuntimeException("User does not have a Stripe customer account");
        }

        try {
            return stripeService.attachPaymentMethodToCustomer(user.getStripeCustomerId(), paymentMethodId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to add payment method: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deductFromSellerEarnings(Long sellerId, BigDecimal amount, Long orderId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
                
        // Create a negative payment record to track the deduction
        Payment deduction = Payment.builder()
                .user(seller)
                .amount(amount.negate()) // Negative amount for deduction
                .currency("USD")
                .paymentMethod("refund_deduction")
                .status("COMPLETED")
                .build();
                
        payRepo.save(deduction);
        
        // If the seller has a Stripe account, also reflect this in Stripe
        if (seller.getStripeCustomerId() != null && !seller.getStripeCustomerId().isEmpty()) {
            try {
                stripeService.deductFromSellerBalance(
                    seller.getStripeCustomerId(), 
                    amount, 
                    "Refund deduction for order #" + orderId
                );
            } catch (StripeException e) {
                // Log the error but don't fail the transaction
                System.err.println("Failed to process Stripe deduction: " + e.getMessage());
            }
        }
    }
}
