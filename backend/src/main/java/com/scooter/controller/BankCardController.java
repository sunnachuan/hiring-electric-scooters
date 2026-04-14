package com.scooter.controller;

import com.scooter.dto.BankCardDTO;
import com.scooter.entity.BankCard;
import com.scooter.service.BankCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 银行卡管理控制器
 */
@RestController
@RequestMapping("/api/bank-cards")
@RequiredArgsConstructor
public class BankCardController {
    
    private final BankCardService bankCardService;
    
    /**
     * 获取用户的银行卡列表
     */
    @GetMapping
    public ResponseEntity<List<BankCard>> getUserBankCards(@RequestAttribute("userId") Long userId) {
        List<BankCard> bankCards = bankCardService.getUserBankCards(userId);
        return ResponseEntity.ok(bankCards);
    }
    
    /**
     * 添加银行卡
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addBankCard(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BankCardDTO bankCardDTO) {
        try {
            BankCard bankCard = bankCardService.addBankCard(userId, bankCardDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "银行卡添加成功");
            response.put("data", bankCard);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新银行卡
     */
    @PutMapping("/{cardId}")
    public ResponseEntity<Map<String, Object>> updateBankCard(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody BankCardDTO bankCardDTO) {
        try {
            BankCard bankCard = bankCardService.updateBankCard(userId, cardId, bankCardDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "银行卡更新成功");
            response.put("data", bankCard);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除银行卡
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Map<String, Object>> deleteBankCard(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long cardId) {
        try {
            bankCardService.deleteBankCard(userId, cardId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "银行卡删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 设置默认银行卡
     */
    @PostMapping("/{cardId}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultCard(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long cardId) {
        try {
            bankCardService.setDefaultCard(userId, cardId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "默认银行卡设置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取默认银行卡
     */
    @GetMapping("/default")
    public ResponseEntity<BankCard> getDefaultBankCard(@RequestAttribute("userId") Long userId) {
        return bankCardService.getDefaultBankCard(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}