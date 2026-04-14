package com.scooter.service;

import com.scooter.dto.BankCardDTO;
import com.scooter.dto.TemporaryUserDTO;
import com.scooter.entity.BankCard;
import com.scooter.entity.TemporaryUser;
import com.scooter.entity.User;
import com.scooter.repository.TemporaryUserRepository;
import com.scooter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 临时用户服务
 */
@Service
@RequiredArgsConstructor
public class TemporaryUserService {
    
    private final TemporaryUserRepository temporaryUserRepository;
    private final BankCardService bankCardService;
    private final UserService userService;
    
    /**
     * 创建临时用户
     */
    @Transactional
    public TemporaryUser createTemporaryUser(TemporaryUserDTO temporaryUserDTO, Long createdBy) {
        // 验证手机号格式
        if (!SecurityUtils.isValidPhone(temporaryUserDTO.getPhone())) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        // 验证身份证号格式（如果提供）
        if (temporaryUserDTO.getIdCard() != null && !temporaryUserDTO.getIdCard().isEmpty()) {
            if (!SecurityUtils.isValidIdCard(temporaryUserDTO.getIdCard())) {
                throw new RuntimeException("身份证号格式不正确");
            }
        }
        
        // 验证紧急联系人手机号格式（如果提供）
        if (temporaryUserDTO.getEmergencyPhone() != null && !temporaryUserDTO.getEmergencyPhone().isEmpty()) {
            if (!SecurityUtils.isValidPhone(temporaryUserDTO.getEmergencyPhone())) {
                throw new RuntimeException("紧急联系人手机号格式不正确");
            }
        }
        
        // 检查手机号是否已存在
        if (temporaryUserRepository.existsByPhoneAndStatus(temporaryUserDTO.getPhone(), "ACTIVE")) {
            throw new RuntimeException("该手机号已存在");
        }
        
        // 注意：临时用户和正式用户是独立的，不检查手机号是否已注册为正式用户
        
        TemporaryUser temporaryUser = new TemporaryUser();
        temporaryUser.setRealName(temporaryUserDTO.getRealName());
        
        // 加密存储手机号
        String encryptedPhone = SecurityUtils.encrypt(temporaryUserDTO.getPhone());
        temporaryUser.setPhone(encryptedPhone);
        
        temporaryUser.setIdCard(temporaryUserDTO.getIdCard());
        temporaryUser.setEmergencyContact(temporaryUserDTO.getEmergencyContact());
        
        // 加密存储紧急联系人手机号
        if (temporaryUserDTO.getEmergencyPhone() != null && !temporaryUserDTO.getEmergencyPhone().isEmpty()) {
            String encryptedEmergencyPhone = SecurityUtils.encrypt(temporaryUserDTO.getEmergencyPhone());
            temporaryUser.setEmergencyPhone(encryptedEmergencyPhone);
        }
        
        temporaryUser.setCreatedBy(createdBy);
        temporaryUser.setCreatedByName(temporaryUserDTO.getCreatedByName());
        
        // 处理银行卡信息
        if (temporaryUserDTO.getBankCard() != null) {
            BankCardDTO bankCardDTO = temporaryUserDTO.getBankCard();
            
            // 创建银行卡（临时用户ID为0，后续更新）
            BankCard bankCard = bankCardService.addBankCard(0L, bankCardDTO);
            temporaryUser.setBankCardId(bankCard.getId());
        }
        
        return temporaryUserRepository.save(temporaryUser);
    }
    
    /**
     * 更新临时用户最后使用时间
     */
    @Transactional
    public void updateLastUsedTime(Long userId) {
        temporaryUserRepository.updateLastUsedTime(userId);
    }
    
    /**
     * 根据手机号查找临时用户
     */
    public Optional<TemporaryUser> findByPhone(String phone) {
        return temporaryUserRepository.findByPhoneAndStatus(phone, "ACTIVE");
    }
    
    /**
     * 根据创建店员查找临时用户
     */
    public List<TemporaryUser> findByCreatedBy(Long createdBy) {
        return temporaryUserRepository.findByCreatedByAndStatusOrderByLastUsedAtDesc(createdBy, "ACTIVE");
    }
    
    /**
     * 查找所有临时用户
     */
    public List<TemporaryUser> findAllActive() {
        return temporaryUserRepository.findByStatusOrderByLastUsedAtDesc("ACTIVE");
    }
    
    /**
     * 删除临时用户（软删除）
     */
    @Transactional
    public void deleteTemporaryUser(Long userId) {
        Optional<TemporaryUser> optionalUser = temporaryUserRepository.findById(userId);
        if (optionalUser.isPresent()) {
            TemporaryUser user = optionalUser.get();
            user.setStatus("INACTIVE");
            
            // 同时删除关联的银行卡
            if (user.getBankCardId() != null) {
                bankCardService.deleteBankCard(0L, user.getBankCardId());
            }
            
            temporaryUserRepository.save(user);
        }
    }
}