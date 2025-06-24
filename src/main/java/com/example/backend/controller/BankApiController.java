package com.example.backend.controller;

import com.example.backend.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
@Tag(name = "Bank API", description = "은행 결제 시스템 API")
public class BankApiController {

    private final BankService bankService;

    // 결제 승인 요청 API
    @PostMapping("/payment/approve")
    @Operation(
        summary = "결제 승인 요청",
        description = "카드 또는 계좌를 통한 결제 승인을 요청합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 승인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "approvalNumber": "AP123456789",
                        "amount": 15000,
                        "method": "card",
                        "message": "결제가 승인되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "success": false,
                        "message": "결제 승인에 실패했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> approvePayment(
            @Parameter(
                description = "결제 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "결제 요청 예시",
                        value = """
                        {
                            "method": "card",
                            "amount": 15000,
                            "cardOrAccountNumber": "1234-5678-9012-3456"
                        }
                        """
                    )
                )
            )
            @RequestBody Map<String, Object> paymentInfo) {
        
        String method = (String) paymentInfo.get("method");
        Integer amount = (Integer) paymentInfo.get("amount");
        String cardOrAccountNumber = (String) paymentInfo.get("cardOrAccountNumber");
        
        Map<String, Object> result = bankService.requestPaymentApproval(method, amount, cardOrAccountNumber);
        
        return ResponseEntity.ok(result);
    }

    // 결제 취소 요청 API
    @PostMapping("/payment/cancel")
    @Operation(
        summary = "결제 취소 요청",
        description = "승인된 결제를 취소합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "approvalNumber": "AP123456789",
                        "message": "결제가 취소되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "success": false,
                        "message": "결제 취소에 실패했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> cancelPayment(
            @Parameter(
                description = "결제 취소 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "결제 취소 요청 예시",
                        value = """
                        {
                            "approvalNumber": "AP123456789"
                        }
                        """
                    )
                )
            )
            @RequestBody Map<String, Object> cancelInfo) {
        
        String approvalNumber = (String) cancelInfo.get("approvalNumber");
        
        Map<String, Object> result = bankService.requestPaymentCancellation(approvalNumber);
        
        return ResponseEntity.ok(result);
    }
}