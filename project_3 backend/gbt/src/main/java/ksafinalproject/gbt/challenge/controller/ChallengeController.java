package ksafinalproject.gbt.challenge.controller;

import io.swagger.annotations.Api;
import ksafinalproject.gbt.challenge.dto.IChallenge;
import ksafinalproject.gbt.challenge.dto.MyChallenge;
import ksafinalproject.gbt.challenge.dto.MyChallengeInfo;
import ksafinalproject.gbt.challenge.dto.OChallenge;
import ksafinalproject.gbt.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Api(tags = {"챌린지"})
@RestController
@CrossOrigin
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {
    private final ChallengeService challengeService;

    @PostMapping("")
    public int challengeSave(IChallenge iChallenge) {
        try {
            return challengeService.saveChallenge(iChallenge);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @PutMapping("/{id}")
    public int challengeUpdate(IChallenge iChallenge, @PathVariable Long id) {
        try {
            return challengeService.updateChallenge(iChallenge, id);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @GetMapping("/{id}")
    public Optional<OChallenge> challengeGetById(@PathVariable Long id) {
        try {
            return challengeService.getChallengeById(id);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @GetMapping("/user/{userId}")
    public Optional<MyChallenge> challengeGetByUserId(@PathVariable Long userId) {
        try {
            return challengeService.getChallengeByUserId(userId);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @GetMapping("/my/{id}")
    public Optional<MyChallengeInfo> myChallengeGetById(@PathVariable Long id) {
        try {
            return challengeService.getMyChallengeById(id);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @GetMapping("/all")
    public List<OChallenge> challengeGetAll() {
        try {
            return challengeService.getAllChallenge();
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("/all/{userId}")
    public List<OChallenge> challengeGetAllIncludeUserId(@PathVariable Long userId) {
        try {
            return challengeService.getAllChallengeIncludeUserId(userId);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public int challengeDeleteById(@PathVariable Long id) {
        try {
            return challengeService.deleteChallengeById(id);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }
}
