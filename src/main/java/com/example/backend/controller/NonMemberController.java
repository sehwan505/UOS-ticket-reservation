package com.example.backend.controller;

import com.example.backend.entity.NonMemberEntity;
import com.example.backend.service.NonMemberService;
import com.example.backend.dto.NonMemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Non-Member API", description = "비회원 관리 API")
public class NonMemberController {

    private final NonMemberService nonMemberService;

    @GetMapping("/nonmembers")
    @Operation(
        summary = "비회원 목록 조회",
        description = "전체 비회원 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "비회원 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NonMemberDto.class)
            )
        )
    })
    public ResponseEntity<List<NonMemberDto>> getAllNonMembers() {
        List<NonMemberDto> allNonMembers = nonMemberService.findAllNonMembers();
        return ResponseEntity.ok(allNonMembers);
    }
}