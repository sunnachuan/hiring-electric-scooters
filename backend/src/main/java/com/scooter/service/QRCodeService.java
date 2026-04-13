package com.scooter.service;

import com.scooter.entity.Scooter;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    
    private final ScooterRepository scooterRepository;
    
    /**
     * 验证二维码并解锁滑板车
     */
    @Transactional
    public Scooter unlockScooter(String qrCode, String unlockCode) {
        Scooter scooter = scooterRepository.findByQrCode(qrCode);
        
        if (scooter == null) {
            throw new RuntimeException("无效的二维码");
        }
        
        if (!scooter.getUnlockCode().equals(unlockCode)) {
            throw new RuntimeException("解锁码错误");
        }
        
        if (!scooter.getIsLocked()) {
            throw new RuntimeException("滑板车已解锁");
        }
        
        if (scooter.getAvailableQuantity() <= 0) {
            throw new RuntimeException("滑板车不可用");
        }
        
        if (scooter.getBatteryLevel() < 10.0) {
            throw new RuntimeException("电量过低，无法解锁");
        }
        
        // 解锁滑板车
        scooter.setIsLocked(false);
        scooter.setAvailableQuantity(scooter.getAvailableQuantity() - 1);
        scooter.setLastUpdateTime(LocalDateTime.now());
        
        return scooterRepository.save(scooter);
    }
    
    /**
     * 锁定滑板车
     */
    @Transactional
    public Scooter lockScooter(String qrCode) {
        Scooter scooter = scooterRepository.findByQrCode(qrCode);
        
        if (scooter == null) {
            throw new RuntimeException("无效的二维码");
        }
        
        if (scooter.getIsLocked()) {
            throw new RuntimeException("滑板车已锁定");
        }
        
        // 锁定滑板车
        scooter.setIsLocked(true);
        scooter.setAvailableQuantity(scooter.getAvailableQuantity() + 1);
        scooter.setLastUpdateTime(LocalDateTime.now());
        
        return scooterRepository.save(scooter);
    }
    
    /**
     * 生成新的二维码和解锁码
     */
    @Transactional
    public Scooter regenerateQRCode(Long scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        // 生成新的二维码和解锁码
        String newQRCode = "SCOOTER_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        String newUnlockCode = String.format("%06d", (int)(Math.random() * 1000000));
        
        scooter.setQrCode(newQRCode);
        scooter.setUnlockCode(newUnlockCode);
        scooter.setLastUpdateTime(LocalDateTime.now());
        
        return scooterRepository.save(scooter);
    }
    
    /**
     * 获取滑板车解锁状态
     */
    public Scooter getScooterStatus(String qrCode) {
        Scooter scooter = scooterRepository.findByQrCode(qrCode);
        
        if (scooter == null) {
            throw new RuntimeException("无效的二维码");
        }
        
        return scooter;
    }
}