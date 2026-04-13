package com.scooter.repository;

import com.scooter.entity.OperationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationTaskRepository extends JpaRepository<OperationTask, Long> {
    
    List<OperationTask> findByStatus(String status);
    
    List<OperationTask> findByTaskType(String taskType);
    
    List<OperationTask> findByAssignedOperatorId(Long operatorId);
    
    List<OperationTask> findByScooterId(Long scooterId);
    
    @Query("SELECT t FROM OperationTask t WHERE t.status IN :statuses")
    List<OperationTask> findByStatusIn(@Param("statuses") List<String> statuses);
    
    @Query("SELECT t FROM OperationTask t WHERE t.priority = :priority AND t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<OperationTask> findPendingTasksByPriority(@Param("priority") String priority);
    
    @Query("SELECT COUNT(t) FROM OperationTask t WHERE t.scooter.id = :scooterId AND t.taskType = :taskType AND t.status IN :statuses")
    boolean existsByScooterIdAndTaskTypeAndStatusIn(@Param("scooterId") Long scooterId, 
                                                   @Param("taskType") String taskType,
                                                   @Param("statuses") List<String> statuses);
    
    // @Query("SELECT o.name, COUNT(t), AVG(t.actualDuration) " +
    //        "FROM OperationTask t JOIN t.assignedOperator o WHERE t.status = 'COMPLETED' AND t.completedAt >= :sinceDate " +
    //        "GROUP BY o.id, o.name")
    // List<Object[]> getOperatorTaskStatistics(@Param("sinceDate") java.time.LocalDateTime sinceDate);
    
    // @Query("SELECT t.taskType, COUNT(t) FROM OperationTask t WHERE t.createdAt >= :startDate GROUP BY t.taskType")
    // List<Object[]> getTaskTypeStatistics(@Param("startDate") java.time.LocalDateTime startDate);
}