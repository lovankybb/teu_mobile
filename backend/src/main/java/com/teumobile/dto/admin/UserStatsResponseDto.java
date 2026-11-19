package com.teumobile.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponseDto {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
}
