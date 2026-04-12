package com.scooter.controller;

import com.scooter.dto.BankCardRequest;
import com.scooter.entity.BankCard;
import com.scooter.entity.User;
import com.scooter.service.BankCardService;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bank-cards")
@RequiredArgsConstructor
public class BankCardController {
    
    private final BankCardService bankCardService;
    private final SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<List<BankCard>> getUserBankCards(HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        List<BankCard> bankCards = bankCardService.getUserBankCards(user.getId());
        return ResponseEntity.ok(bankCards);
    }
    
    @PostMapping
    public ResponseEntity<BankCard> addBankCard(@Valid @RequestBody BankCardRequest bankCardRequest,
                                                HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        BankCard bankCard = bankCardService.addBankCard(user, bankCardRequest);
        return ResponseEntity.ok(bankCard);
    }
    
    @PutMapping("/{cardId}/default")
    public ResponseEntity<BankCard> setDefaultCard(@PathVariable Long cardId,
                                                   HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        BankCard bankCard = bankCardService.setDefaultCard(user.getId(), cardId);
        return ResponseEntity.ok(bankCard);
    }
    
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteBankCard(@PathVariable Long cardId,
                                               HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        bankCardService.deleteBankCard(user.getId(), cardId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/default")
    public ResponseEntity<BankCard> getDefaultCard(HttpServletRequest request) {
        User user = securityUtils.getCurrentUser(request);
        return bankCardService.getDefaultCard(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}