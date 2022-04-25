package ksafinalproject.gbt.smoking.service;

import ksafinalproject.gbt.challenge.model.Challenge;
import ksafinalproject.gbt.challenge.repository.ChallengeRepository;
import ksafinalproject.gbt.smoking.dto.ISmoking;
import ksafinalproject.gbt.smoking.dto.OSmoking;
import ksafinalproject.gbt.smoking.dto.TotalSmoking;
import ksafinalproject.gbt.smoking.dto.TotalSmokingAndDays;
import ksafinalproject.gbt.smoking.model.Smoking;
import ksafinalproject.gbt.smoking.repository.SmokingRepository;
import ksafinalproject.gbt.user.model.User;
import ksafinalproject.gbt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class SmokingServiceImpl implements SmokingService {

    private final SmokingRepository smokingRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Override
    @Transactional
    public Long saveSmoking(ISmoking iSmoking) {
        log.info("save smoking : {} ", iSmoking);
        try {
            LocalDate now = LocalDate.now();
            try {
                Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, iSmoking.getUserId());
                smoking.orElseThrow().setCount(smoking.orElseThrow().getCount() + 1);
                return smoking.orElseThrow().getCount();
            } catch (Exception ignored) {
            }
            smokingRepository.save(
                    Smoking.builder()
                            .id(iSmoking.getId())
                            .count(1L)
                            .date(LocalDate.now())
                            .user(userRepository.findById(iSmoking.getUserId()).orElseThrow())
                            .isAttend(false)
                            .build());
            return 1L;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1L;
        }
    }

    @Transactional
    @Override
    public Long saveAttendSmoking(ISmoking iSmoking) {
        log.info("attend smoking : {}", iSmoking);
        try {
            LocalDate now = LocalDate.now();
            try {
                Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, iSmoking.getUserId());
                if (iSmoking.getCount() <= smoking.orElseThrow().getCount()) {
                    return -2L;
                }
                smoking.orElseThrow().setCount(iSmoking.getCount());
                if (!smoking.orElseThrow().getIsAttend()) {
                    Optional<User> user = userRepository.findById(iSmoking.getUserId());
                    user.orElseThrow().setPoint(user.orElseThrow().getPoint() + 100);
                    smoking.orElseThrow().setIsAttend(true);
                }
                return smoking.orElseThrow().getCount();
            } catch (Exception ignored) {
            }
            smokingRepository.save(
                    Smoking.builder()
                            .id(iSmoking.getId())
                            .count(iSmoking.getCount())
                            .date(LocalDate.now())
                            .user(userRepository.findById(iSmoking.getUserId()).orElseThrow())
                            .isAttend(true)
                            .build());
            Optional<User> user = userRepository.findById(iSmoking.getUserId());
            user.orElseThrow().setPoint(user.orElseThrow().getPoint() + 100);
            return iSmoking.getCount();
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1L;
        }
    }

    @Transactional
    @Override
    public int saveChallengeAttendSmoking(ISmoking iSmoking) {
        log.info("challenge attend smoking : {}", iSmoking);
        try {
            if (iSmoking.getMemo() == null) {
                return -1;
            }
            LocalDate now = LocalDate.now();
            try {
                Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, iSmoking.getUserId());
                if (smoking.orElseThrow().getCount() >= 1) {
                    return 4;
                }
                if (smoking.orElseThrow().getMemo() == null) {
                    Optional<User> user = userRepository.findById(iSmoking.getUserId());
                    user.orElseThrow().setPoint(user.orElseThrow().getPoint() + 50);
                    smoking.orElseThrow().setMemo(iSmoking.getMemo());
                    return 1;
                } else {
                    return 3;
                }
            } catch (Exception ignored) {
            }
            smokingRepository.save(
                    Smoking.builder()
                            .id(iSmoking.getId())
                            .count(0L)
                            .date(LocalDate.now())
                            .user(userRepository.findById(iSmoking.getUserId()).orElseThrow())
                            .isAttend(false)
                            .memo(iSmoking.getMemo())
                            .build());
            Optional<User> user = userRepository.findById(iSmoking.getUserId());
            user.orElseThrow().setPoint(user.orElseThrow().getPoint() + 50);
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @Transactional
    @Override
    public int updateSmoking(ISmoking iSmoking, Long id) {
        log.info("update smoking : {}, id : {}", iSmoking, id);
        try {
            Smoking smoking2 = smokingRepository.findById(id).orElseThrow();
            smoking2.setCount(iSmoking.getCount());
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @Override
    public Optional<OSmoking> getSmokingById(Long id) {
        log.info("find smoking by id : {}", id);
        try {
            Optional<Smoking> smoking = smokingRepository.findById(id);
            OSmoking oSmoking = OSmoking.builder()
                    .id(smoking.orElseThrow().getId())
                    .count(smoking.orElseThrow().getCount())
                    .date(smoking.orElseThrow().getDate())
                    .userId(smoking.orElseThrow().getUser().getId())
                    .isAttend(smoking.orElseThrow().getIsAttend())
                    .memo(smoking.orElseThrow().getMemo())
                    .build();
            return Optional.of(oSmoking);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<OSmoking> getAllSmoking() {
        log.info("find all smoking");
        try {
            List<Smoking> smokingList = smokingRepository.findAll();
            List<OSmoking> oSmokingList = new ArrayList<>();
            for (Smoking smoking : smokingList) {
                oSmokingList.add(OSmoking.builder()
                        .id(smoking.getId())
                        .count(smoking.getCount())
                        .date(smoking.getDate())
                        .userId(smoking.getUser().getId())
                        .isAttend(smoking.getIsAttend())
                        .memo(smoking.getMemo())
                        .build());
            }
            return oSmokingList;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int deleteSmokingById(Long id) {
        log.info("delete smoking by id : {}", id);
        try {
            smokingRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return -1;
        }
    }

    @Override
    public Optional<OSmoking> getSmokingByUserId(Long userId) {
        log.info("find smoking by userid : {}", userId);
        try {
            Optional<Smoking> smoking = smokingRepository.findSmokingByUserId(userId);
            OSmoking oSmoking = OSmoking.builder()
                    .id(smoking.orElseThrow().getId())
                    .count(smoking.orElseThrow().getCount())
                    .date(smoking.orElseThrow().getDate())
                    .userId(smoking.orElseThrow().getUser().getId())
                    .isAttend(smoking.orElseThrow().getIsAttend())
                    .memo(smoking.orElseThrow().getMemo())
                    .build();
            return Optional.of(oSmoking);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<OSmoking> getTodaySmokingByUserId(Long userId) {
        log.info("find today smoking by userid : {}", userId);
        try {
            LocalDate now = LocalDate.now();
            Optional<Smoking> smoking = smokingRepository.findByDateAndUserId(now, userId);
            OSmoking oSmoking = OSmoking.builder()
                    .id(smoking.orElseThrow().getId())
                    .count(smoking.orElseThrow().getCount())
                    .date(smoking.orElseThrow().getDate())
                    .userId(smoking.orElseThrow().getUser().getId())
                    .isAttend(smoking.orElseThrow().getIsAttend())
                    .memo(smoking.orElseThrow().getMemo())
                    .build();
            return Optional.of(oSmoking);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<OSmoking> getAllSmokingByUserId(Long userId) {
        log.info("find all smoking by user id {}", userId);
        try {
            List<Smoking> smokingList = smokingRepository.findAllByUserId(userId);
            List<OSmoking> oSmokingList = new ArrayList<>();
            for (Smoking smoking : smokingList) {
                oSmokingList.add(OSmoking.builder()
                        .id(smoking.getId())
                        .count(smoking.getCount())
                        .date(smoking.getDate())
                        .userId(smoking.getUser().getId())
                        .isAttend(smoking.getIsAttend())
                        .memo(smoking.getMemo())
                        .build());
            }
            return oSmokingList;
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<TotalSmoking> getSmokingByDate(Long day, Long userId) {
        log.info("find all smoking by day {}, user id {}", day, userId);
        try {
            List<Smoking> smokingList = smokingRepository.findAllByUserId(userId);
            List<OSmoking> result = new ArrayList<>();
            Long total = 0L;
            TotalSmoking smokingDto = new TotalSmoking();
            LocalDate now = LocalDate.now();
            for (Smoking smoking : smokingList) {
                if (smoking.getDate().isAfter(now.minusDays(day))) {
                    result.add(OSmoking.builder()
                            .id(smoking.getId())
                            .count(smoking.getCount())
                            .date(smoking.getDate())
                            .userId(smoking.getUser().getId())
                            .isAttend(smoking.getIsAttend())
                            .memo(smoking.getMemo())
                            .build());
                    total += smoking.getCount();
                }
            }
            smokingDto.setSmokingList(result);
            smokingDto.setTotal(total);
            return Optional.of(smokingDto);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<TotalSmoking> getSmokingByMonth(Long userId) {
        log.info("find all this month smoking by user id {}", userId);
        try {
            List<Smoking> smokingList = smokingRepository.findAllByUserId(userId);
            List<OSmoking> result = new ArrayList<>();
            Long total = 0L;
            TotalSmoking smokingDto = new TotalSmoking();
            LocalDate now = LocalDate.now();
            for (Smoking smoking : smokingList) {
                if (smoking.getDate().getMonthValue() == now.getMonthValue() && smoking.getDate().getYear() == now.getYear()) {
                    result.add(OSmoking.builder()
                            .id(smoking.getId())
                            .count(smoking.getCount())
                            .date(smoking.getDate())
                            .userId(smoking.getUser().getId())
                            .isAttend(smoking.getIsAttend())
                            .memo(smoking.getMemo())
                            .build());
                    total += smoking.getCount();
                }
            }
            smokingDto.setSmokingList(result);
            smokingDto.setTotal(total);
            return Optional.of(smokingDto);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<TotalSmokingAndDays> getSmokingByUserAndChallengeDate(Long userId, Long challengeId) {
        log.info("find smoking by user and challenge");
        try {
            Optional<Challenge> challenge = challengeRepository.findById(challengeId);
            LocalDate startDate = challenge.orElseThrow().getStartDate().toLocalDate();
            LocalDate endDate = challenge.orElseThrow().getEndDate().toLocalDate();
            List<Smoking> smokingList = smokingRepository.findByDateBetweenAndUserId(startDate, endDate, userId);
            Long total = 0L;
            long totalAttends = 0L;
            long totalMemos = 0L;
            Long period = ChronoUnit.DAYS.between(startDate, endDate);
            long smokingDays = smokingList.size();
            for (Smoking smoking : smokingList) {
                total += smoking.getCount();
                if (smoking.getCount() != 0) {
                    smokingDays += 1;
                }
                if (smoking.getIsAttend()) {
                    totalAttends += 1;
                }
                if (smoking.getMemo() != null) {
                    totalMemos += 1;
                }
            }
            TotalSmokingAndDays totalSmokingAndDays = TotalSmokingAndDays.builder()
                    .total(total)
                    .smokingDays(smokingDays)
                    .period(period)
                    .totalAttends(totalAttends)
                    .totalMemos(totalMemos)
                    .build();
            return Optional.of(totalSmokingAndDays);
        } catch (Exception e) {
            log.error("Error : {}", e.getMessage());
            return Optional.empty();
        }
    }
}