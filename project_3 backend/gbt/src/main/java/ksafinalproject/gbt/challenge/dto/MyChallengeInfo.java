package ksafinalproject.gbt.challenge.dto;

import ksafinalproject.gbt.challengeImg.model.ChallengeImg;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyChallengeInfo {
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String method;
    private Long frequency;
    private String description;
    private Long startingPeople;
    private Long currentPeople;
    private Long todayProofPeople;
    private Long max;
    private Long point;
    private List<ChallengeImg> challengeImg;
}
