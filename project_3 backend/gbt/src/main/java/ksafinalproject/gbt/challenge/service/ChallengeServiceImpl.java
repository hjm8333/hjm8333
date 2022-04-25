package ksafinalproject.gbt.challenge.service;

import ksafinalproject.gbt.S3Uploader;
import ksafinalproject.gbt.challenge.dto.IChallenge;
import ksafinalproject.gbt.challenge.dto.MyChallenge;
import ksafinalproject.gbt.challenge.dto.MyChallengeInfo;
import ksafinalproject.gbt.challenge.dto.OChallenge;
import ksafinalproject.gbt.challenge.model.Challenge;
import ksafinalproject.gbt.challenge.repository.ChallengeRepository;
import ksafinalproject.gbt.challengeImg.model.ChallengeImg;
import ksafinalproject.gbt.challengeImg.repository.ChallengeImgRepository;
import ksafinalproject.gbt.smoking.model.Smoking;
import ksafinalproject.gbt.smoking.repository.SmokingRepository;
import ksafinalproject.gbt.user.model.User;
import ksafinalproject.gbt.user.repository.UserRepository;
import ksafinalproject.gbt.userChallenge.model.UserChallenge;
import ksafinalproject.gbt.userChallenge.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeImgRepository challengeImgRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;
    private final SmokingRepository smokingRepository;
    private final S3Uploader s3Uploader;

    @Override
    public int saveChallenge(IChallenge iChallenge) {
        log.info("save challenge : {}", iChallenge);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startDate = LocalDateTime.parse(iChallenge.getStartDate(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(iChallenge.getEndDate(), formatter);
            if (iChallenge.getImg() == null) {
                challengeRepository.save(Challenge.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .method(iChallenge.getMethod())
                        .title(iChallenge.getTitle())
                        .frequency(iChallenge.getFrequency())
                        .summary(iChallenge.getSummary())
                        .description(iChallenge.getDescription())
                        .max(iChallenge.getMax())
                        .startingPeople(0L)
                        .point(iChallenge.getPoint())
                        .isStart(false)
                        .build());
                return 1;
            }
            Long challengeId = challengeRepository.save(Challenge.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .method(iChallenge.getMethod())
                    .title(iChallenge.getTitle())
                    .frequency(iChallenge.getFrequency())
                    .summary(iChallenge.getSummary())
                    .description(iChallenge.getDescription())
                    .max(iChallenge.getMax())
                    .startingPeople(0L)
                    .point(iChallenge.getPoint())
                    .isStart(false)
                    .build()).getId();

            List<MultipartFile> imageFiles = iChallenge.getImg();
            imageFiles.forEach(file -> {
                try {
                    String url = (s3Uploader.upload(file, "challengeImg"));
                    ChallengeImg img = new ChallengeImg();
                    img.setChallenge(challengeRepository.getById(challengeId));
                    img.setUrl(url);
                    System.out.println(">>> " + img.toString());
                    challengeImgRepository.save(img);
                } catch (Exception e) {
                    log.error("Error : {}", e.getMessage());
                }
            });
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @Override
    @Transactional
    public int updateChallenge(IChallenge iChallenge, Long id) {
        log.info("update challenge : {}, id : {}", iChallenge, id);
        try {
            if (challengeRepository.findById(id).isEmpty()) {
                return -1;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startDate = LocalDateTime.parse(iChallenge.getStartDate(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(iChallenge.getEndDate(), formatter);
            Challenge challenge = challengeRepository.findById(id).orElseThrow();
            challenge.setStartDate(startDate);
            challenge.setEndDate(endDate);
            challenge.setTitle(iChallenge.getTitle());
            challenge.setFrequency(iChallenge.getFrequency());
            challenge.setDescription(iChallenge.getDescription());
            challenge.setSummary(iChallenge.getSummary());
            challenge.setMax(iChallenge.getMax());
            challenge.setPoint(iChallenge.getPoint());
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @Override
    @Transactional
    public void checkChallengeStart() {
        LocalDateTime now = LocalDateTime.now();
        log.info("checking challenge start. time : {}", now);
        try {
            List<Challenge> challengeList = challengeRepository.findAll();
            for (Challenge challenge : challengeList) {
                if (now.isAfter(challenge.getStartDate()) && !challenge.getIsStart()) {
                    Long current = userChallengeRepository.countByChallengeId(challenge.getId());
                    challenge.setIsStart(true);
                    challenge.setStartingPeople(current);
                }
            }
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void checkChallengeEnd() {
        LocalDateTime now = LocalDateTime.now();
        log.info("checking challenge end. time : {}", now);
        try {
            List<Challenge> challengeList = challengeRepository.findAll();
            for (Challenge challenge : challengeList) {
                if (now.isAfter(challenge.getEndDate())) {
                    List<UserChallenge> userChallengeList = userChallengeRepository.findAllByChallengeId(challenge.getId());
                    for (UserChallenge userChallenge : userChallengeList) {
                        User user = userChallenge.getUser();
                        user.setCompletedChallenge(user.getCompletedChallenge() + 1);
                        user.setPoint(user.getPoint() + challenge.getPoint());
                    }
                    challengeRepository.deleteById(challenge.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void checkChallengeMemoCheck() {
        LocalDate now = LocalDate.now();
        log.info("checking challenge fail by memo : {}", now);
        try {
            List<Challenge> challengeList = challengeRepository.findAll();
            for (Challenge challenge : challengeList) {
                if (challenge.getIsStart()) {
                    List<UserChallenge> userChallengeList = userChallengeRepository.findAllByChallengeId(challenge.getId());
                    for (UserChallenge userChallenge : userChallengeList) {
                        boolean exist = smokingRepository.existsByDateAndUserId(now, userChallenge.getUser().getId());
                        if (!exist) {
                            User user = userChallenge.getUser();
                            user.setFailedChallenge(user.getFailedChallenge() + 1);
                            userChallengeRepository.deleteById(userChallenge.getId());
                        }
                        try {
                            Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, userChallenge.getUser().getId());
                            if (smoking.orElseThrow().getMemo() == null) {
                                User user = userChallenge.getUser();
                                user.setFailedChallenge(user.getFailedChallenge() + 1);
                                userChallengeRepository.deleteById(userChallenge.getId());
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }
    }

    @Override
    public Optional<OChallenge> getChallengeById(Long id) {
        log.info("find challenge by id : {}", id);
        try {
            Optional<Challenge> challenge = challengeRepository.findById(id);
            OChallenge oChallenge = new OChallenge();
            Long current = userChallengeRepository.countByChallengeId(challenge.orElseThrow().getId());
            oChallenge.setId(challenge.orElseThrow().getId());
            oChallenge.setTitle(challenge.orElseThrow().getTitle());
            oChallenge.setStartDate(challenge.orElseThrow().getStartDate());
            oChallenge.setEndDate(challenge.orElseThrow().getEndDate());
            oChallenge.setMethod(challenge.orElseThrow().getMethod());
            oChallenge.setFrequency(challenge.orElseThrow().getFrequency());
            oChallenge.setDescription(challenge.orElseThrow().getDescription());
            oChallenge.setSummary(challenge.orElseThrow().getSummary());
            oChallenge.setCurrent(current);
            oChallenge.setMax(challenge.orElseThrow().getMax());
            oChallenge.setStartingPeople(challenge.orElseThrow().getStartingPeople());
            oChallenge.setPoint(challenge.orElseThrow().getPoint());
            oChallenge.setIsStart(challenge.orElseThrow().getIsStart());
            oChallenge.setChallengeImg(challenge.orElseThrow().getChallengeImg());
            oChallenge.setProof(challenge.orElseThrow().getProof());
            oChallenge.setUserChallenge(challenge.orElseThrow().getUserChallenge());
            return Optional.of(oChallenge);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<MyChallenge> getChallengeByUserId(Long userId) {
        log.info("find challenge by user id : {}", userId);
        try {
            Optional<UserChallenge> userChallenge = userChallengeRepository.findByUserId(userId);
            Optional<Challenge> challenge = challengeRepository.findById(userChallenge.orElseThrow().getChallenge().getId());
            Long current = userChallengeRepository.countByChallengeId(challenge.orElseThrow().getId());
            MyChallenge myChallenge = MyChallenge.builder()
                    .id(challenge.orElseThrow().getId())
                    .title(challenge.orElseThrow().getTitle())
                    .startDate(challenge.orElseThrow().getStartDate())
                    .endDate(challenge.orElseThrow().getEndDate())
                    .method(challenge.orElseThrow().getMethod())
                    .frequency(challenge.orElseThrow().getFrequency())
                    .summary(challenge.orElseThrow().getSummary())
                    .description(challenge.orElseThrow().getDescription())
                    .currentPeople(current)
                    .max(challenge.orElseThrow().getMax())
                    .startingPeople(challenge.orElseThrow().getStartingPeople())
                    .point(challenge.orElseThrow().getPoint())
                    .build();
            return Optional.of(myChallenge);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<MyChallengeInfo> getMyChallengeById(Long id) {
        log.info("find my challenge by id : {}", id);
        try {
            Optional<Challenge> challenge = challengeRepository.findById(id);
            Long current = userChallengeRepository.countByChallengeId(challenge.orElseThrow().getId());
            long proofPeople = 0L;
            LocalDate now = LocalDate.now();
            List<UserChallenge> userChallengeList = userChallengeRepository.findAllByChallengeId(id);
            for (UserChallenge userChallenge : userChallengeList) {
                try {
                    Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, userChallenge.getUser().getId());
                    if (smoking.orElseThrow().getMemo() != null) {
                        proofPeople++;
                    }
                } catch (Exception ignored) {
                }
            }
            MyChallengeInfo myChallengeInfo = MyChallengeInfo.builder()
                    .id(challenge.orElseThrow().getId())
                    .title(challenge.orElseThrow().getTitle())
                    .startDate(challenge.orElseThrow().getStartDate())
                    .endDate(challenge.orElseThrow().getEndDate())
                    .method(challenge.orElseThrow().getMethod())
                    .frequency(challenge.orElseThrow().getFrequency())
                    .description(challenge.orElseThrow().getDescription())
                    .startingPeople(challenge.orElseThrow().getStartingPeople())
                    .currentPeople(current)
                    .todayProofPeople(proofPeople)
                    .max(challenge.orElseThrow().getMax())
                    .point(challenge.orElseThrow().getPoint())
                    .challengeImg(challenge.orElseThrow().getChallengeImg())
                    .build();
            return Optional.of(myChallengeInfo);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<OChallenge> getAllChallenge() {
        log.info("find all challenge");
        try {
            List<Challenge> challengeList = challengeRepository.findAll();
            List<OChallenge> oChallenge = new ArrayList<>();
            for (Challenge challenge : challengeList) {
                Long current = userChallengeRepository.countByChallengeId(challenge.getId());
                oChallenge.add(OChallenge.builder()
                        .id(challenge.getId())
                        .title(challenge.getTitle())
                        .startDate(challenge.getStartDate())
                        .endDate(challenge.getEndDate())
                        .method(challenge.getMethod())
                        .frequency(challenge.getFrequency())
                        .summary(challenge.getSummary())
                        .description(challenge.getDescription())
                        .current(current)
                        .max(challenge.getMax())
                        .startingPeople(challenge.getStartingPeople())
                        .point(challenge.getPoint())
                        .isStart(challenge.getIsStart())
                        .challengeImg(challenge.getChallengeImg())
                        .proof(challenge.getProof())
                        .userChallenge(challenge.getUserChallenge())
                        .build());
            }
            return oChallenge;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<OChallenge> getAllChallengeIncludeUserId(Long userId) {
        log.info("find all challenge include user id : {}", userId);
        try {
            List<Challenge> challengeList = challengeRepository.findAll();
            List<OChallenge> oChallenge = new ArrayList<>();
            for (Challenge challenge : challengeList) {
                Long current = userChallengeRepository.countByChallengeId(challenge.getId());
                oChallenge.add(OChallenge.builder()
                        .id(challenge.getId())
                        .title(challenge.getTitle())
                        .startDate(challenge.getStartDate())
                        .endDate(challenge.getEndDate())
                        .method(challenge.getMethod())
                        .frequency(challenge.getFrequency())
                        .summary(challenge.getSummary())
                        .description(challenge.getDescription())
                        .isJoin(userChallengeRepository.existsByUserIdAndChallengeId(userId, challenge.getId()))
                        .current(current)
                        .max(challenge.getMax())
                        .startingPeople(challenge.getStartingPeople())
                        .point(challenge.getPoint())
                        .isStart(challenge.getIsStart())
                        .challengeImg(challenge.getChallengeImg())
                        .proof(challenge.getProof())
                        .userChallenge(challenge.getUserChallenge())
                        .build());
            }
            return oChallenge;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int deleteChallengeById(Long id) {
        log.info("delete challenge by id : {}", id);
        try {
            challengeRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }
}
