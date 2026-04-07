package com.example.demo.log.service;

import com.example.demo.entity.DetailType;
import com.example.demo.entity.Log;
import com.example.demo.entity.LogDetail;
import com.example.demo.entity.User;
import com.example.demo.log.dto.LogDetailResponseDto;
import com.example.demo.log.dto.LogResponseDto;
import com.example.demo.log.repository.LogDetailRepository;
import com.example.demo.log.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final LogDetailRepository logDetailRepository;

    /* 업무 기록 목록 조회 로직 */
    public List<LogResponseDto> getDailyLogs(User user, LocalDate date) {
        return logRepository.findByUserAndDateOrderByStartTimeDesc(user, date)
                .stream()
                .map(LogResponseDto::from) // Entity를 DTO로 변환
                .collect(Collectors.toList());
    }

    /* 업무 기록 상세 조회 로직 */
    @Transactional(readOnly = true)
    public LogDetailResponseDto getLogDetail(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        List<LogDetail> details = logDetailRepository.findByLog(log);

        return LogDetailResponseDto.builder()
                .title(log.getTitle())
                .date(log.getDate().toString())
                .summaries(filterContent(details, DetailType.SUMMARY))
                .decisions(filterContent(details, DetailType.DECISION))
                .todos(details.stream()
                        .filter(d -> d.getType() == DetailType.TODO)
                        .map(d -> new LogDetailResponseDto.TodoDto(d.getAssignee(), d.getContent()))
                        .collect(Collectors.toList()))
                .build();
    }

    private List<String> filterContent(List<LogDetail> details, DetailType type) {
        return details.stream()
                .filter(d -> d.getType() == type)
                .map(LogDetail::getContent)
                .collect(Collectors.toList());
    }

}
