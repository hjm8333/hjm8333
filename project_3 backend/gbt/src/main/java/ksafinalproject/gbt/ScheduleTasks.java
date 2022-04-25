package ksafinalproject.gbt;

import ksafinalproject.gbt.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@Slf4j
public class ScheduleTasks {

    private final ChallengeService challengeService;

    @Scheduled(cron = "1 1,29,31,59 * ? * *")
    public void challengeStartCheck() {
        log.info("scheduler start. time : {}", LocalDateTime.now());
        try {
            challengeService.checkChallengeStart();
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }
    @Scheduled(cron = "5 58,59 23 ? * *")
    public void challengeMemoCheck() {
        log.info("scheduler start. time : {}", LocalDateTime.now());
        try {
            challengeService.checkChallengeMemoCheck();
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }
    @Scheduled(cron = "15,25 59 23 ? * *")
    public void challengeEndCheck() {
        log.info("scheduler start. time : {}", LocalDateTime.now());
        try {
            challengeService.checkChallengeEnd();
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }
}
