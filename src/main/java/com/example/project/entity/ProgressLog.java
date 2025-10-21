package com.example.project.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "progress_log")
public class ProgressLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "old_progress")
    private Integer oldProgress;

    @Column(name = "new_progress")
    private Integer newProgress;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "changed_time")
    private LocalDateTime changedTime = LocalDateTime.now();
}