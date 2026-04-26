package com.scooter.controller;

import com.scooter.dto.BankCardDTO;
import com.scooter.entity.BankCard;
import com.scooter.entity.User;
import com.scooter.service.BankCardService;
import com.scooter.util.DataPermissionValidator;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
@Slf4j
public class BankCardController {
    
    private final BankCardService bankCardService;
    private final SecurityUtils securityUtils;
    private final DataPermissionValidator dataPermissionValidator;
    
    /**
     * 获取用户的银行卡列表
     */
    @GetMapping
    public ResponseEntity<List<BankCard>> getUserBankCards(HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        List<BankCard> bankCards = bankCardService.getUserBankCards(user.getId());
        return ResponseEntity.ok(bankCards);
    }
    
    /**
     * 添加银行卡
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addBankCard(
            HttpServletRequest request,
            @Valid @RequestBody BankCardDTO bankCardDTO) {
        try {
            User user = securityUtils.getCurrentUser(request);
            BankCard bankCard = bankCardService.addBankCard(user.getId(), bankCardDTO);
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
            HttpServletRequest request,
            @PathVariable Long cardId,
            @Valid @RequestBody BankCardDTO bankCardDTO) {
        try {
            User user = securityUtils.getCurrentUser(request);
            BankCard bankCard = bankCardService.updateBankCard(user.getId(), cardId, bankCardDTO);
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
            HttpServletRequest request,
            @PathVariable Long cardId) {
        try {
            User user = securityUtils.getCurrentUser(request);
            bankCardService.deleteBankCard(user.getId(), cardId);
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
            HttpServletRequest request,
            @PathVariable Long cardId) {
        try {
            User user = securityUtils.getCurrentUser(request);
            bankCardService.setDefaultCard(user.getId(), cardId);
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
    public ResponseEntity<BankCard> getDefaultBankCard(HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        return bankCardService.getDefaultBankCard(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}