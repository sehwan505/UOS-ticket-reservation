package com.example.backend.controller;

import com.example.backend.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankApiController {

    private final BankService bankService;

    // 결제 승인 요청 API
    @PostMapping("/payment/approve")
    public ResponseEntity<Map<String, Object>> approvePayment(
            @RequestBody Map<String, Object> paymentInfo) {
        
        String method = (String) paymentInfo.get("method");
        Integer amount = (Integer) paymentInfo.get("amount");
        String cardOrAccountNumber = (String) paymentInfo.get("cardOrAccountNumber");
        
        Map<String, Object> result = bankService.requestPaymentApproval(method, amount, cardOrAccountNumber);
        
        return ResponseEntity.ok(result);
    }

    // 결제 취소 요청 API
    @PostMapping("/payment/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(
            @RequestBody Map<String, Object> cancelInfo) {
        
        String approvalNumber = (String) cancelInfo.get("approvalNumber");
        
        Map<String, Object> result = bankService.requestPaymentCancellation(approvalNumber);
        
        return ResponseEntity.ok(result);
    }
}