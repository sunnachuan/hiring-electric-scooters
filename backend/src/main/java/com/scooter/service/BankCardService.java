package com.scooter.service;

import com.scooter.dto.BankCardRequest;
import com.scooter.entity.BankCard;
import com.scooter.entity.User;
import com.scooter.repository.BankCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankCardService {
    
    private final BankCardRepository bankCardRepository;
    
    public List<BankCard> getUserBankCards(Long userId) {
        return bankCardRepository.findByUserIdOrderByIsDefaultDescLastUsedAtDesc(userId);
    }
    
    @Transactional
    public BankCard addBankCard(User user, BankCardRequest request) {
        // 检查卡号是否已存在
        if (bankCardRepository.existsByUserIdAndCardNumber(user.getId(), request.getCardNumber())) {
            throw new RuntimeException("该银行卡已存在");
        }
        
        // 简化验证：只验证卡号基本格式
        if (request.getCardNumber() == null || request.getCardNumber().trim().isEmpty()) {
            throw new RuntimeException("银行卡号不能为空");
        }
        
        // 如果是第一张卡，设置为默认
        List<BankCard> existingCards = bankCardRepository.findByUserIdOrderByIsDefaultDescLastUsedAtDesc(user.getId());
        boolean isFirstCard = existingCards.isEmpty();
        
        BankCard bankCard = new BankCard();
        bankCard.setUser(user);
        bankCard.setCardNumber(request.getCardNumber());
        bankCard.setCardHolderName(request.getCardHolderName());
        bankCard.setExpiryMonth(request.getExpiryMonth());
        bankCard.setExpiryYear(request.getExpiryYear());
        bankCard.setCvv(request.getCvv());
        bankCard.setIsDefault(isFirstCard);
        bankCard.setCreatedAt(LocalDateTime.now());
        
        return bankCardRepository.save(bankCard);
    }
    
    @Transactional
    public BankCard setDefaultCard(Long userId, Long cardId) {
        // 获取当前默认卡并取消默认
        Optional<BankCard> currentDefault = bankCardRepository.findByUserIdAndIsDefaultTrue(userId);
        if (currentDefault.isPresent()) {
            BankCard defaultCard = currentDefault.get();
            defaultCard.setIsDefault(false);
            bankCardRepository.save(defaultCard);
        }
        
        // 设置新的默认卡
        BankCard newDefault = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("银行卡不存在"));
        
        if (!newDefault.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此银行卡");
        }
        
        newDefault.setIsDefault(true);
        return bankCardRepository.save(newDefault);
    }
    
    @Transactional
    public void deleteBankCard(Long userId, Long cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("银行卡不存在"));
        
        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此银行卡");
        }
        
        // 如果是默认卡，需要设置另一张卡为默认
        if (card.getIsDefault()) {
            List<BankCard> otherCards = bankCardRepository.findByUserIdOrderByIsDefaultDescLastUsedAtDesc(userId);
            otherCards.remove(card);
            
            if (!otherCards.isEmpty()) {
                BankCard newDefault = otherCards.get(0);
                newDefault.setIsDefault(true);
                bankCardRepository.save(newDefault);
            }
        }
        
        bankCardRepository.delete(card);
    }
    
    public Optional<BankCard> getDefaultCard(Long userId) {
        return bankCardRepository.findByUserIdAndIsDefaultTrue(userId);
    }
    
    @Transactional
    public void updateLastUsedTime(Long cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("银行卡不存在"));
        
        card.setLastUsedAt(LocalDateTime.now());
        bankCardRepository.save(card);
    }
    
    private boolean isValidCardNumber(String cardNumber) {
        // 简单的卡号格式验证
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        // 移除空格和连字符
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // 检查是否为数字且长度在12-19位之间
        if (!cleanCardNumber.matches("\\d{12,19}")) {
            return false;
        }
        
        // 简单的Luhn算法验证
        return isValidLuhn(cleanCardNumber);
    }
    
    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
}