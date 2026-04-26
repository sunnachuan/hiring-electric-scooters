package com.scooter.service;

import com.scooter.dto.BankCardDTO;
import com.scooter.entity.BankCard;
import com.scooter.repository.BankCardRepository;
import com.scooter.util.DataPermissionValidator;
import com.scooter.util.EncryptionUtils;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 银行卡服务
 */
@Service
@RequiredArgsConstructor
public class BankCardService {
    
    private final BankCardRepository bankCardRepository;
    private final EncryptionUtils encryptionUtils;
    private final DataPermissionValidator dataPermissionValidator;
    
    /**
     * 获取用户的银行卡列表
     */
    public List<BankCard> getUserBankCards(Long userId) {
        return bankCardRepository.findByUserIdAndStatusOrderByIsDefaultDescCreatedAtDesc(userId, "ACTIVE");
    }
    
    /**
     * 添加银行卡
     */
    @Transactional
    public BankCard addBankCard(Long userId, BankCardDTO bankCardDTO) {
        // 验证银行卡号格式
        if (!encryptionUtils.isValidCardNumber(bankCardDTO.getCardNumber())) {
            throw new RuntimeException("银行卡号格式不正确");
        }
        
        // 检查银行卡号是否已存在
        if (bankCardRepository.existsByUserIdAndCardNumberAndStatus(userId, bankCardDTO.getCardNumber(), "ACTIVE")) {
            throw new RuntimeException("该银行卡已存在");
        }
        
        BankCard bankCard = new BankCard();
        bankCard.setUserId(userId);
        
        // 加密存储银行卡号
        String encryptedCardNumber = encryptionUtils.encrypt(bankCardDTO.getCardNumber());
        bankCard.setCardNumber(encryptedCardNumber);
        
        // 生成显示卡号（只显示后4位）
        String displayNumber = encryptionUtils.generateCardNumberDisplay(bankCardDTO.getCardNumber());
        bankCard.setCardNumberDisplay(displayNumber);
        
        bankCard.setBankName(bankCardDTO.getBankName());
        bankCard.setCardholderName(bankCardDTO.getCardholderName());
        bankCard.setCardType(bankCardDTO.getCardType());
        bankCard.setExpiryDate(bankCardDTO.getExpiryDate());
        
        // 如果是第一张卡或用户指定为默认，则设置为默认
        Long cardCount = bankCardRepository.countByUserIdAndStatus(userId, "ACTIVE");
        if (cardCount == 0 || Boolean.TRUE.equals(bankCardDTO.getIsDefault())) {
            bankCard.setIsDefault(true);
        }
        
        // 如果设置为默认，取消其他卡的默认设置
        if (Boolean.TRUE.equals(bankCard.getIsDefault())) {
            bankCardRepository.unsetOtherDefaultCards(userId, 0L);
        }
        
        return bankCardRepository.save(bankCard);
    }
    
    /**
     * 更新银行卡
     */
    @Transactional
    public BankCard updateBankCard(Long userId, Long cardId, BankCardDTO bankCardDTO) {
        Optional<BankCard> optionalCard = bankCardRepository.findByIdAndUserId(cardId, userId);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("银行卡不存在");
        }
        
        BankCard bankCard = optionalCard.get();
        
        // 检查银行卡号是否与其他卡冲突（排除当前卡）
        if (!bankCard.getCardNumber().equals(bankCardDTO.getCardNumber()) &&
            bankCardRepository.existsByUserIdAndCardNumberAndStatus(userId, bankCardDTO.getCardNumber(), "ACTIVE")) {
            throw new RuntimeException("该银行卡已存在");
        }
        
        bankCard.setCardNumber(bankCardDTO.getCardNumber());
        
        // 更新显示卡号
        String cardNumber = bankCardDTO.getCardNumber();
        String displayNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        bankCard.setCardNumberDisplay(displayNumber);
        
        bankCard.setBankName(bankCardDTO.getBankName());
        bankCard.setCardholderName(bankCardDTO.getCardholderName());
        bankCard.setCardType(bankCardDTO.getCardType());
        bankCard.setExpiryDate(bankCardDTO.getExpiryDate());
        
        // 处理默认设置
        if (Boolean.TRUE.equals(bankCardDTO.getIsDefault()) && !bankCard.getIsDefault()) {
            bankCard.setIsDefault(true);
            bankCardRepository.unsetOtherDefaultCards(userId, cardId);
        }
        
        return bankCardRepository.save(bankCard);
    }
    
    /**
     * 删除银行卡
     */
    @Transactional
    public void deleteBankCard(Long userId, Long cardId) {
        Optional<BankCard> optionalCard = bankCardRepository.findByIdAndUserId(cardId, userId);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("银行卡不存在");
        }
        
        BankCard bankCard = optionalCard.get();
        
        // 如果是默认卡，需要设置另一张卡为默认
        if (bankCard.getIsDefault()) {
            List<BankCard> otherCards = bankCardRepository.findByUserIdAndStatusOrderByIsDefaultDescCreatedAtDesc(userId, "ACTIVE");
            otherCards.removeIf(card -> card.getId().equals(cardId));
            
            if (!otherCards.isEmpty()) {
                BankCard newDefaultCard = otherCards.get(0);
                newDefaultCard.setIsDefault(true);
                bankCardRepository.save(newDefaultCard);
            }
        }
        
        // 软删除：设置状态为INACTIVE
        bankCard.setStatus("INACTIVE");
        bankCardRepository.save(bankCard);
    }
    
    /**
     * 设置默认银行卡
     */
    @Transactional
    public void setDefaultCard(Long userId, Long cardId) {
        Optional<BankCard> optionalCard = bankCardRepository.findByIdAndUserId(cardId, userId);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("银行卡不存在");
        }
        
        // 取消其他卡的默认设置
        bankCardRepository.unsetOtherDefaultCards(userId, cardId);
        
        // 设置当前卡为默认
        int updated = bankCardRepository.setAsDefaultCard(cardId, userId);
        if (updated == 0) {
            throw new RuntimeException("设置默认卡失败");
        }
    }
    
    /**
     * 获取默认银行卡
     */
    public Optional<BankCard> getDefaultBankCard(Long userId) {
        return bankCardRepository.findByUserIdAndIsDefaultTrueAndStatus(userId, "ACTIVE");
    }
}