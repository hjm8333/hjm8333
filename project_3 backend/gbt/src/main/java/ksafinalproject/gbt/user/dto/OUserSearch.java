package ksafinalproject.gbt.user.dto;

import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OUserSearch {
    private Long id;
    private String userName;
    private String profileImg;
}
