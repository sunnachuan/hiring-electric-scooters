package com.scooter.service;

import com.scooter.entity.Operator;
import com.scooter.entity.OperationTask;
import com.scooter.entity.Scooter;
import com.scooter.repository.OperatorRepository;
import com.scooter.repository.OperationTaskRepository;
import com.scooter.repository.ScooterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationService {
    
    private final OperatorRepository operatorRepository;
    private final OperationTaskRepository taskRepository;
    private final ScooterRepository scooterRepository;
    private final DeviceService deviceService;
    
    /**
     * 创建充电任务
     */
    @Transactional
    public OperationTask createChargingTask(Long scooterId, String priority) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        OperationTask task = new OperationTask();
        task.setTaskType("CHARGING");
        task.setPriority(priority);
        task.setScooter(scooter);
        task.setDescription("为滑板车 " + scooter.getModel() + " (ID: " + scooterId + ") 充电");
        task.setEstimatedDuration(120); // 预计2小时
        
        return taskRepository.save(task);
    }
    
    /**
     * 创建部署任务
     */
    @Transactional
    public OperationTask createDeploymentTask(Long scooterId, String targetLocation, 
                                             Double targetLatitude, Double targetLongitude) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        OperationTask task = new OperationTask();
        task.setTaskType("DEPLOYMENT");
        task.setScooter(scooter);
        task.setTargetLocation(targetLocation);
        task.setTargetLatitude(targetLatitude);
        task.setTargetLongitude(targetLongitude);
        task.setDescription("部署滑板车 " + scooter.getModel() + " 到 " + targetLocation);
        task.setEstimatedDuration(30); // 预计30分钟
        
        return taskRepository.save(task);
    }
    
    /**
     * 创建收集任务
     */
    @Transactional
    public OperationTask createCollectionTask(Long scooterId, String reason) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        OperationTask task = new OperationTask();
        task.setTaskType("COLLECTION");
        task.setScooter(scooter);
        task.setDescription("收集滑板车 " + scooter.getModel() + " (ID: " + scooterId + ") - " + reason);
        task.setEstimatedDuration(45); // 预计45分钟
        
        return taskRepository.save(task);
    }
    
    /**
     * 创建维修任务
     */
    @Transactional
    public OperationTask createMaintenanceTask(Long scooterId, String issueDescription, String priority) {
        Scooter scooter = scooterRepository.findById(scooterId)
                .orElseThrow(() -> new RuntimeException("滑板车不存在: " + scooterId));
        
        OperationTask task = new OperationTask();
        task.setTaskType("MAINTENANCE");
        task.setPriority(priority);
        task.setScooter(scooter);
        task.setDescription("维修滑板车 " + scooter.getModel() + " (ID: " + scooterId + ") - " + issueDescription);
        task.setEstimatedDuration(60); // 预计1小时
        
        return taskRepository.save(task);
    }
    
    /**
     * 分配任务给运维人员
     */
    @Transactional
    public OperationTask assignTask(Long taskId, Long operatorId) {
        OperationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("运维人员不存在: " + operatorId));
        
        // 检查运维人员是否可用
        if (!"ACTIVE".equals(operator.getStatus())) {
            throw new RuntimeException("运维人员不可用");
        }
        
        // 检查运维人员当前任务数量
        if (operator.getCurrentTaskCount() >= 3) { // 每人最多同时处理3个任务
            throw new RuntimeException("运维人员任务已满");
        }
        
        task.setAssignedOperator(operator);
        task.setStatus("ASSIGNED");
        task.setAssignedAt(LocalDateTime.now());
        
        // 更新运维人员任务计数
        operator.setCurrentTaskCount(operator.getCurrentTaskCount() + 1);
        operator.setLastActiveTime(LocalDateTime.now());
        
        operatorRepository.save(operator);
        return taskRepository.save(task);
    }
    
    /**
     * 开始执行任务
     */
    @Transactional
    public OperationTask startTask(Long taskId) {
        OperationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        if (!"ASSIGNED".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不正确，无法开始");
        }
        
        task.setStatus("IN_PROGRESS");
        task.setStartedAt(LocalDateTime.now());
        
        return taskRepository.save(task);
    }
    
    /**
     * 完成任务
     */
    @Transactional
    public OperationTask completeTask(Long taskId, String completionNotes) {
        OperationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不正确，无法完成");
        }
        
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionNotes(completionNotes);
        
        // 计算实际耗时
        if (task.getStartedAt() != null) {
            long minutes = java.time.Duration.between(task.getStartedAt(), LocalDateTime.now()).toMinutes();
            task.setActualDuration((int) minutes);
        }
        
        // 更新运维人员任务计数
        if (task.getAssignedOperator() != null) {
            Operator operator = task.getAssignedOperator();
            operator.setCurrentTaskCount(Math.max(0, operator.getCurrentTaskCount() - 1));
            operator.setTotalTasksCompleted(operator.getTotalTasksCompleted() + 1);
            operator.setLastActiveTime(LocalDateTime.now());
            operatorRepository.save(operator);
        }
        
        // 根据任务类型更新滑板车状态
        updateScooterStatusAfterTask(task);
        
        return taskRepository.save(task);
    }
    
    /**
     * 根据任务类型更新滑板车状态
     */
    private void updateScooterStatusAfterTask(OperationTask task) {
        if (task.getScooter() == null) return;
        
        Scooter scooter = task.getScooter();
        
        switch (task.getTaskType()) {
            case "CHARGING":
                scooter.setBatteryLevel(100.0); // 充满电
                scooter.setStatus("AVAILABLE");
                break;
            case "MAINTENANCE":
                scooter.setStatus("AVAILABLE");
                break;
            case "DEPLOYMENT":
                if (task.getTargetLatitude() != null && task.getTargetLongitude() != null) {
                    scooter.setLatitude(task.getTargetLatitude());
                    scooter.setLongitude(task.getTargetLongitude());
                }
                scooter.setStatus("AVAILABLE");
                break;
            case "COLLECTION":
                scooter.setStatus("MAINTENANCE"); // 收集后需要维护
                break;
        }
        
        scooterRepository.save(scooter);
    }
    
    /**
     * 获取待处理任务列表
     */
    public List<OperationTask> getPendingTasks() {
        return taskRepository.findByStatus("PENDING");
    }
    
    /**
     * 获取进行中任务列表
     */
    public List<OperationTask> getInProgressTasks() {
        return taskRepository.findByStatus("IN_PROGRESS");
    }
    
    /**
     * 获取运维人员任务统计
     */
    // public List<Object[]> getOperatorTaskStats() {
    //     return taskRepository.getOperatorTaskStatistics(java.time.LocalDateTime.now().minusDays(30));
    // }
    
    /**
     * 自动创建低电量充电任务
     */
    @Transactional
    public void createLowBatteryChargingTasks(Double threshold) {
        List<Scooter> lowBatteryScooters = deviceService.getLowBatteryScooters(threshold);
        
        for (Scooter scooter : lowBatteryScooters) {
            // 检查是否已有充电任务
            boolean hasChargingTask = taskRepository.existsByScooterIdAndTaskTypeAndStatusIn(
                    scooter.getId(), "CHARGING", List.of("PENDING", "ASSIGNED", "IN_PROGRESS"));
            
            if (!hasChargingTask) {
                String priority = scooter.getBatteryLevel() < 10.0 ? "URGENT" : "HIGH";
                createChargingTask(scooter.getId(), priority);
            }
        }
    }
}