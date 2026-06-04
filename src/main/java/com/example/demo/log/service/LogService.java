package com.example.demo.log.service;

import com.example.demo.entity.*;
import com.example.demo.log.dto.LogCreateRequest;
import com.example.demo.log.dto.LogDetailResponseDto;
import com.example.demo.log.dto.LogResponseDto;
import com.example.demo.log.repository.LogDetailRepository;
import com.example.demo.log.repository.LogRepository;
import com.example.demo.users.repository.UserRepository;
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
    private final UserRepository userRepository;

    /* 업무 기록 목록 조회 로직 */
    public List<LogResponseDto> getDailyLogs(String email, LocalDate date) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return logRepository.findByUserAndDateOrderByStartTimeDesc(user, date)
                .stream()
                .map(LogResponseDto::from)
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

    /* 업무 기록 생성 및 요약 로직 */
    @Transactional
    public LogDetailResponseDto createLog(String email, LogCreateRequest request) {

        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 메인 로그 생성 및 저장
        Log log = Log.builder()
                .user(user)
                .title(request.getTitle())
                .date(request.getStartTime().toLocalDate())
                .startTime(request.getStartTime().toLocalTime())
                .duration(request.getDuration())
                .type(LogType.CONFERENCE) // 기본값 설정
                .build();

        Log savedLog = logRepository.save(log);

        // AI 요약 실행 (실제로는 여기서 외부 AI API를 호출
        // 지금은 테스트를 위해 임시 메서드로 처리
        generateAiDetails(savedLog, request.getRawContent());

        return getLogDetail(savedLog.getId());
    }

    private void generateAiDetails(Log log, String rawContent) {

        // TODO: 나중에 여기서 OpenAI나 Clova 등의 API를 호출하여 결과를 파싱
        // 현재는 피그마 데이터 기반으로 샘플 데이터를 저장하는 로직

        List<LogDetail> details = List.of(
                LogDetail.builder().log(log).content("메인 페이지 UI 완료").type(DetailType.SUMMARY).assignee(null).build(),
                LogDetail.builder().log(log).content("이번 주 명세 확정").type(DetailType.DECISION).assignee(null).build(),
                LogDetail.builder().log(log).content("응답 형식 정리").type(DetailType.TODO).assignee("참여자 3").build()

        );

        logDetailRepository.saveAll(details);
    }

    /* 메모 추가 및 수정 로직 */
    @Transactional
    public void updateMemo(Long logId, String memo) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));

        log.updateMemo(memo); // Dirty Checking 으로 자동 업데이트
    }
}
