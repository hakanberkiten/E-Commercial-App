package com.example.e_commerce.service;

import com.example.e_commerce.entity.Orders;

public interface EmailService {
    void sendOrderConfirmation(Orders order);
}