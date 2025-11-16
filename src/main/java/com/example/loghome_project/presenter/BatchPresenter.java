package com.example.loghome_project.presenter;

import com.example.loghome_project.model.BatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MVP 패턴: Presenter
 * 배치 파일 실행을 처리하는 비즈니스 로직
 */
@Component
public class BatchPresenter {

    private static final Logger logger = LoggerFactory.getLogger(BatchPresenter.class);
    private static final String BATCH_FILE_PATH = "getCurrentTime.bat";
    
    @Autowired
    private Environment environment;

    /**
     * 배치 파일을 실행하고 결과를 반환
     * @return BatchResult 모델 객체
     */
    public BatchResult executeBatch() {
        logger.debug("[A단계] BatchPresenter.executeBatch() 메서드 진입");
        
        // Active Profile 확인
        logger.debug("[B단계] Environment에서 Active Profile 조회");
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        logger.info("현재 Active Profile: {}", profile);
        logger.debug("[B단계 완료] Profile 확인됨: {}", profile);
        
        // Profile에 따른 로그 디렉토리 및 파일명 설정
        logger.debug("[C단계] Profile 기반 로그 디렉토리 설정");
        String logDirectory = profile.equals("prod") ? "logs/batch-prod" : "logs/batch-dev";
        String logFileName = profile.equals("prod") ? "batch-prod" : "batch-dev";
        logger.debug("[C단계 완료] 로그 디렉토리: {}, 파일명: {}", logDirectory, logFileName);
        
        // 로그 디렉토리 생성
        logger.debug("[D단계] 로그 디렉토리 생성 시도");
        try {
            Path logDir = Paths.get(logDirectory);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("로그 디렉토리 생성: {}", logDirectory);
            } else {
                logger.debug("[D단계 완료] 로그 디렉토리 이미 존재: {}", logDirectory);
            }
        } catch (IOException e) {
            logger.error("로그 디렉토리 생성 실패: {}", e.getMessage());
            logger.debug("[D단계 실패] IOException 발생");
        }
        
        logger.info("배치 파일 실행 시작: {} (Profile: {})", BATCH_FILE_PATH, profile);
        logger.debug("[E단계] 배치 파일 실행 준비: {}", BATCH_FILE_PATH);
        
        StringBuilder result = new StringBuilder();
        
        // Profile 정보를 환경변수로 전달
        logger.debug("[F단계] ProcessBuilder 생성 및 환경변수 설정");
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", BATCH_FILE_PATH);
        processBuilder.environment().put("CURRENT_PROFILE", profile);
        logger.debug("[F단계 완료] CURRENT_PROFILE 환경변수 설정: {}", profile);
        
        try {
            // 배치 파일 실행
            logger.debug("[G단계] 배치 파일 프로세스 시작");
            Process process = processBuilder.start();
            logger.info("배치 프로세스가 실행되었습니다. PID: {}", process.pid());
            logger.debug("[G단계 완료] 프로세스 PID: {}", process.pid());

            // 실행 결과 읽기
            logger.debug("[H단계] 배치 실행 결과 스트림 읽기 시작");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "EUC-KR")
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[배치 실행 결과] {}", line);
                logger.debug("[H단계] 결과 라인 읽음: {}", line);
                result.append(line).append("\n");
            }
            logger.debug("[H단계 완료] 스트림 읽기 완료");

            // 에러 스트림 읽기
            logger.debug("[I단계] 에러 스트림 읽기 시작");
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), "EUC-KR")
            );
            
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                logger.error("[배치 실행 에러] {}", errorLine);
                logger.debug("[I단계] 에러 라인 읽음: {}", errorLine);
                result.append("[ERROR] ").append(errorLine).append("\n");
            }
            logger.debug("[I단계 완료] 에러 스트림 읽기 완료");

            // 프로세스 종료 대기
            logger.debug("[J단계] 프로세스 종료 대기");
            int exitCode = process.waitFor();
            
            boolean success = (exitCode == 0);
            String message = success ? "배치 파일이 성공적으로 실행되었습니다." : "배치 파일 실행이 비정상적으로 종료되었습니다.";
            
            logger.info("{} 종료 코드: {}", message, exitCode);
            logger.debug("[J단계 완료] 종료 코드: {}, 성공여부: {}", exitCode, success);
            
            // 결과를 파일로 저장
            logger.debug("[K단계] 배치 실행 결과 파일 저장 시작");
            saveBatchResultToFile(logDirectory, logFileName, result.toString(), profile, exitCode);
            logger.debug("[K단계 완료] 파일 저장 완료");
            
            logger.debug("[L단계] BatchResult 객체 생성 및 반환");
            return new BatchResult(message, result.toString(), exitCode, success);

        } catch (IOException e) {
            logger.error("배치 파일 실행 중 IOException 발생: {}", e.getMessage(), e);
            logger.debug("[에러] IOException 발생: {}", e.getMessage());
            return new BatchResult("IOException 발생", e.getMessage(), -1, false);
        } catch (InterruptedException e) {
            logger.error("배치 파일 실행 중 InterruptedException 발생: {}", e.getMessage(), e);
            logger.debug("[에러] InterruptedException 발생: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return new BatchResult("InterruptedException 발생", e.getMessage(), -1, false);
        }
    }
    
    /**
     * 배치 실행 결과를 파일로 저장
     */
    private void saveBatchResultToFile(String logDirectory, String logFileName, String result, String profile, int exitCode) {
        logger.debug("[K-1] saveBatchResultToFile() 메서드 진입");
        
        try {
            logger.debug("[K-2] 타임스탬프 생성");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s_%s.log", logFileName, timestamp, profile);
            Path logFilePath = Paths.get(logDirectory, fileName);
            logger.debug("[K-3] 로그 파일 경로: {}", logFilePath.toString());
            
            logger.debug("[K-4] 로그 내용 생성");
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("================================================\n");
            fileContent.append("배치 실행 결과 로그\n");
            fileContent.append("Profile: ").append(profile).append("\n");
            fileContent.append("실행 시간: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            fileContent.append("종료 코드: ").append(exitCode).append("\n");
            fileContent.append("================================================\n\n");
            fileContent.append(result);
            
            // 파일로 저장
            logger.debug("[K-5] 파일 저장 시작: {}", logFilePath.toString());
            Files.write(logFilePath, fileContent.toString().getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            logger.info("배치 실행 결과가 파일로 저장되었습니다: {}", logFilePath.toString());
            logger.debug("[K-6] 파일 저장 완료");
            
        } catch (IOException e) {
            logger.error("배치 실행 결과 파일 저장 실패: {}", e.getMessage(), e);
            logger.debug("[K단계 실패] IOException 발생: {}", e.getMessage());
        }
    }
}
