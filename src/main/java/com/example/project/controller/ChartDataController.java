package com.example.project.controller;

import com.example.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chart-data")
public class ChartDataController {
    
    @Autowired
    private ProjectService projectService;
    
    // 获取项目进度趋势图表数据
    // 修改参数类型从 Long 到 String
    @GetMapping("/progressTrend")
    public ResponseEntity<List<Map<String, Object>>> getProgressTrend(
            @RequestParam(required = false) String projectId,  // 改为 String 类型
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(required = false) String dateRangeType) {
        
        // 声明开始和结束时间变量
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        
        // 优先处理预设日期范围
        if (dateRangeType != null && !dateRangeType.isEmpty()) {
            switch (dateRangeType.toLowerCase()) {
                case "week":
                    // 周数据：从当天开始向上推七天
                    start = end.minusDays(7);
                    break;
                case "month":
                    // 月数据：从当天开始向上推一个月（约30天）
                    start = end.minusDays(30);
                    break;
                case "year":
                    // 年数据：从当前日期的前一年的同一天开始，确保覆盖12个完整月份
                    start = end.minusYears(1);
                    break;
                default:
                    // 默认查询最近30天
                    start = end.minusDays(30);
            }
        } else if (startDate.isEmpty() || endDate.isEmpty()) {
            // 如果没有提供日期范围，默认查询最近30天的数据
            start = end.minusDays(30);
        } else {
            // 解析用户提供的日期
            start = LocalDateTime.parse(startDate);
            end = LocalDateTime.parse(endDate);
        }
        
        if (projectId != null) {
            // 查询单个项目的进度趋势
            List<Map<String, Object>> trendData = projectService.getProjectProgressTrend(projectId, start, end);
            return new ResponseEntity<>(trendData, HttpStatus.OK);
        } else {
            // 查询所有项目的整体进度趋势
            List<Map<String, Object>> overallTrendData = projectService.getOverallProjectProgressTrend(start, end);
            return new ResponseEntity<>(overallTrendData, HttpStatus.OK);
        }
    }
}