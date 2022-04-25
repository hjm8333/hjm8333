package ksafinalproject.gbt.challenge.service;

import ksafinalproject.gbt.challenge.dto.MyChallenge;
import ksafinalproject.gbt.challenge.dto.MyChallengeInfo;
import ksafinalproject.gbt.challenge.dto.IChallenge;
import ksafinalproject.gbt.challenge.dto.OChallenge;

import java.util.List;
import java.util.Optional;

public interface ChallengeService {
    int saveChallenge(IChallenge iChallenge);

    int updateChallenge(IChallenge iChallenge, Long id);

    void checkChallengeStart();

    void checkChallengeEnd();

    void checkChallengeMemoCheck();

    Optional<OChallenge> getChallengeById(Long id);

    Optional<MyChallenge> getChallengeByUserId(Long userId);

    Optional<MyChallengeInfo> getMyChallengeById(Long id);

    List<OChallenge> getAllChallenge();

    List<OChallenge> getAllChallengeIncludeUserId(Long userId);

//    List<Challenge> getAllChallenge();

    int deleteChallengeById(Long id);

}
