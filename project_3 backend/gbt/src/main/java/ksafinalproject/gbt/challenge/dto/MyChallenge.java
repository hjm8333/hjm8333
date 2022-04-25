package ksafinalproject.gbt.challenge.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyChallenge {
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String method;
    private Long frequency;
    private String summary;
    private String description;
    private Long currentPeople;
    private Long max;
    private Long startingPeople;
    private Long point;
}
