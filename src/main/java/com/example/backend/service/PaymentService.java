package com.example.backend.service;

import com.example.backend.dto.PaymentDto;
import com.example.backend.dto.PaymentSaveDto;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PaymentEntity;
import com.example.backend.entity.PointHistoryEntity;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PointHistoryRepository pointHistoryRepository;
    
    // 모든 결제 조회
    public List<PaymentDto> findAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 결제 상세 조회
    public PaymentDto findPaymentById(String id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + id));
        
        return convertToDto(payment);
    }
    
    // 승인번호로 결제 조회
    public PaymentDto findPaymentByApprovalNumber(String approvalNumber) {
        PaymentEntity payment = paymentRepository.findByApprovalNumber(approvalNumber);
        if (payment == null) {
            return null;
        }
        return convertToDto(payment);
    }
    
    // 결제 등록
    @Transactional
    public String savePayment(PaymentSaveDto paymentSaveDto) {
        // 결제 ID 생성 (UUID)
        String paymentId = UUID.randomUUID().toString();
        
        // 결제 기본 정보 설정
        PaymentEntity payment = PaymentEntity.builder()
                .id(paymentId)
                .method(paymentSaveDto.getMethod())
                .amount(paymentSaveDto.getAmount())
                .paymentTime(LocalDateTime.now())
                .status("N") // 결제미완료로 시작
                .build();
        
        // 포인트 차감 처리
        if (paymentSaveDto.getMemberId() != null && paymentSaveDto.getDeductedPoints() > 0) {
            MemberEntity member = memberRepository.findById(paymentSaveDto.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + paymentSaveDto.getMemberId()));
            
            // 포인트 부족 체크
            if (member.getAvailablePoints() < paymentSaveDto.getDeductedPoints()) {
                throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
            }
            
            // 포인트 차감
            payment.setDeductedPoints(paymentSaveDto.getDeductedPoints());
            member.setAvailablePoints(member.getAvailablePoints() - paymentSaveDto.getDeductedPoints());
            
            // 포인트 사용 내역 기록
            PointHistoryEntity pointHistory = PointHistoryEntity.builder()
                    .member(member)
                    .amount(paymentSaveDto.getDeductedPoints())
                    .type("U") // 사용
                    .build();
            pointHistoryRepository.save(pointHistory);
        } else {
            payment.setDeductedPoints(0);
        }
        
        PaymentEntity savedPayment = paymentRepository.save(payment);
        return savedPayment.getId();
    }
    
    // 결제 진행 상태로 변경
    @Transactional
    public String processPayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + paymentId));
        
        payment.setStatus("D"); // 결제중으로 변경
        
        return payment.getId();
    }
    
    // 결제 완료 처리 (은행/카드사 결제 승인 후)
    @Transactional
    public String completePayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + paymentId));
        
        // 결제 승인번호 생성 (실제로는 외부 서비스에서 받아옴)
        String approvalNumber = generateApprovalNumber();
        
        payment.setStatus("Y"); // 결제완료로 변경
        payment.setApprovalNumber(approvalNumber);
        
        return payment.getId();
    }
    
    // 결제 취소
    @Transactional
    public String cancelPayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + paymentId));
        
        // 포인트 환불
        if (payment.getDeductedPoints() != null && payment.getDeductedPoints() > 0) {
            // 해당 예매와 연결된 회원 찾기
            if (!payment.getReservations().isEmpty() && payment.getReservations().get(0).getMember() != null) {
                MemberEntity member = payment.getReservations().get(0).getMember();
                
                // 포인트 환불
                member.setAvailablePoints(member.getAvailablePoints() + payment.getDeductedPoints());
                
                // 포인트 적립 내역 기록 (환불)
                PointHistoryEntity pointHistory = PointHistoryEntity.builder()
                        .member(member)
                        .amount(payment.getDeductedPoints())
                        .type("A") // 적립 (환불)
                        .build();
                pointHistoryRepository.save(pointHistory);
            }
        }
        
        paymentRepository.delete(payment);
        
        return paymentId;
    }
    
    // 더미 승인번호 생성 (실제 은행/카드사 통신 대체)
    private String generateApprovalNumber() {
        return "AP" + System.currentTimeMillis();
    }
    
    // Entity를 DTO로 변환
    private PaymentDto convertToDto(PaymentEntity payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .method(payment.getMethod())
                .deductedPoints(payment.getDeductedPoints())
                .amount(payment.getAmount())
                .paymentTime(payment.getPaymentTime())
                .status(payment.getStatus())
                .approvalNumber(payment.getApprovalNumber())
                .build();
    }
}