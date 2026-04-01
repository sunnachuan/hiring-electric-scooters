package com.scooter.repository;

import com.scooter.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByPriorityOrderByCreatedAtDesc(String priority);
    List<Feedback> findAllByOrderByCreatedAtDesc();
}