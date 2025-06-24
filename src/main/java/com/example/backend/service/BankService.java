package com.example.backend.service;

import com.example.backend.constants.BusinessConstants;
import com.example.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class BankService {
    
    private final IdGenerator idGenerator;
    
    public BankService(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private final Random random = new Random();
    
    // 결제 승인 요청 (더미)
    public Map<String, Object> requestPaymentApproval(String paymentMethod, int amount, String cardOrAccountNumber) {
        log.info("결제 승인 요청 - 방식: {}, 금액: {}, 번호: {}", paymentMethod, amount, cardOrAccountNumber);
        
        // 실제 은행/카드사 통신 대신 더미 응답
        Map<String, Object> response = new HashMap<>();
        
        // 랜덤으로 승인/실패 결정 (95% 성공률)
        boolean isApproved = random.nextInt(100) < BusinessConstants.Payment.SUCCESS_RATE;
        
        if (isApproved) {
            response.put("status", "SUCCESS");
            response.put("approvalNumber", generateApprovalNumber(paymentMethod));
            response.put("approvedAt", LocalDateTime.now().toString());
            response.put("message", "결제가 승인되었습니다.");
        } else {
            response.put("status", "FAIL");
            response.put("errorCode", idGenerator.generateErrorCode());
            response.put("message", "결제 승인에 실패했습니다. 카드사에 문의하세요.");
        }
        
        log.info("결제 응답: {}", response);
        
        // 실제 통신에서는 여기서 약간의 지연을 주는 것이 좋음
        try {
            Thread.sleep(BusinessConstants.Payment.PROCESSING_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return response;
    }
    
    // 결제 취소 요청 (더미)
    public Map<String, Object> requestPaymentCancellation(String approvalNumber) {
        log.info("결제 취소 요청 - 승인번호: {}", approvalNumber);
        
        // 실제 은행/카드사 통신 대신 더미 응답
        Map<String, Object> response = new HashMap<>();
        
        // 랜덤으로 취소/실패 결정 (98% 성공률)
        boolean isCancelled = random.nextInt(100) < BusinessConstants.Payment.CANCEL_SUCCESS_RATE;
        
        if (isCancelled) {
            response.put("status", "SUCCESS");
            response.put("cancelNumber", "CANCEL" + System.currentTimeMillis());
            response.put("cancelledAt", LocalDateTime.now().toString());
            response.put("message", "결제가 취소되었습니다.");
        } else {
            response.put("status", "FAIL");
            response.put("errorCode", idGenerator.generateErrorCode());
            response.put("message", "결제 취소에 실패했습니다. 카드사에 문의하세요.");
        }
        
        log.info("취소 응답: {}", response);
        
        return response;
    }
    
    // 더미 승인번호 생성
    private String generateApprovalNumber(String paymentMethod) {
        String prefix = paymentMethod.startsWith("CARD") ? "CD" : "BK";
        return idGenerator.generateApprovalNumber(prefix);
    }
}