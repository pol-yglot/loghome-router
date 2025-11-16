package com.example.loghome_project.controller;

import com.example.loghome_project.model.BatchResult;
import com.example.loghome_project.presenter.BatchPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MVP 패턴: View (Controller)
 * REST API 엔드포인트를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    
    @Autowired
    private BatchPresenter batchPresenter;
    
    @Autowired
    private Environment environment;

    /**
     * 홈 엔드포인트 - 로그 테스트
     */
    @GetMapping("/")
    public String home() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        logger.debug("디버그 레벨 로그");
        logger.info("인포 레벨 로그 - Profile: {}", profile);
        logger.warn("경고 레벨 로그");
        logger.error("에러 레벨 로그");
        
        return "로그 설정이 완료되었습니다.\nActive Profile: " + profile + "\n콘솔과 logs/application.log 파일을 확인하세요.";
    }

    /**
     * 테스트 엔드포인트
     */
    @GetMapping("/test")
    public String test() {
        logger.info("테스트 엔드포인트 호출됨");
        return "테스트 성공";
    }

    /**
     * 배치 파일 실행 엔드포인트
     * @return 배치 실행 결과
     */
    @GetMapping("/execute-batch")
    public String executeBatch() {
        logger.debug("[1단계] 배치 실행 엔드포인트 요청 수신");
        
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        logger.debug("[2단계] Active Profile 확인: {}", profile);
        logger.info("배치 파일 실행 엔드포인트 호출됨 - Profile: {}", profile);
        
        logger.debug("[3단계] BatchPresenter.executeBatch() 메서드 호출");
        // Presenter를 통해 비즈니스 로직 실행
        BatchResult result = batchPresenter.executeBatch();
        
        logger.debug("[4단계] 배치 실행 결과 수신 - 성공여부: {}, 종료코드: {}", result.isSuccess(), result.getExitCode());
        
        logger.debug("[5단계] HTTP 응답 생성 및 반환");
        return "Profile: " + profile + "\n" +
               (result.isSuccess() 
                   ? "배치 파일이 성공적으로 실행되었습니다.\n\n실행 결과:\n" + result.getOutput() + "\n종료 코드: " + result.getExitCode()
                   : "배치 파일 실행 중 오류가 발생했습니다.\n\n에러: " + result.getOutput());
    }
    
    /**
     * 현재 Active Profile 확인 엔드포인트
     */
    @GetMapping("/profile")
    public String getProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        
        logger.info("Profile 조회: {}", profile);
        
        return "현재 Active Profile: " + profile;
    }
}
