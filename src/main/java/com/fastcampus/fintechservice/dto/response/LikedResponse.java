package com.fastcampus.fintechservice.dto.response;
import com.fastcampus.fintechservice.db.finance.enums.FinProductType;
import com.fastcampus.fintechservice.db.liked.Liked;
import com.fastcampus.fintechservice.db.lounge.enums.FinancialType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikedResponse {
    private String name;
    private String count;
    private FinProductType finProductType;

    public static LikedResponse from(String name, String count, FinProductType type) {
        return LikedResponse.builder()
                .name(name)
                .count(count)
                .finProductType(type)
                .build();
    }


    public static LikedResponse fromDetail(Liked liked) {
        return LikedResponse.builder()
                .name(getFinancialProductName(liked))
                .finProductType(getFinancialProductType(liked))
                .build();
    }

    private static String getFinancialProductName(Liked liked) {
        if (liked.getDeposit() != null) {
            return liked.getDeposit().getFin_prdt_nm();
        } else {
            return liked.getSaving().getFin_prdt_nm();
        }
    }

    private static FinProductType getFinancialProductType(Liked liked) {
        if (liked.getDeposit() != null) {
            return FinProductType.DEPOSIT;
        } else {
            return FinProductType.SAVING;
        }
    }
}