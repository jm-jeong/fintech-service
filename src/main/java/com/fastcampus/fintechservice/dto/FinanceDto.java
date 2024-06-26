package com.fastcampus.fintechservice.dto;

import static com.fastcampus.fintechservice.common.ImageConverter.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fastcampus.fintechservice.db.finance.Deposit;
import com.fastcampus.fintechservice.db.finance.DepositOption;
import com.fastcampus.fintechservice.db.finance.Saving;
import com.fastcampus.fintechservice.db.finance.SavingOption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDto {

	private String financeId;//depostId or savingId
	private String financeType;//financeType
	private String finPrdtNm;//금융상품명

	private String korCoNm;//은행명
	private String bankImageUrl;//은행이미지 url

	private Double intrRateShow;//저축 금리[소수점2자리]
	private Double intrRate2Show;//최고 우대금리[소수점 2자리]

	private Integer joinMin;//가입기간 최솟값
	private Integer joinMax;//가입기간 최댓값

	private List<FinancePreferenceDto> financePreferenceDtoList;

	public static FinanceDto fromDeposit(Deposit deposit) {
		List<DepositOption> depositOptions = deposit.getDepositOptions();
		List<Integer> saveList = new ArrayList<>();
		for (DepositOption depositOption : depositOptions) {
			saveList.add(depositOption.getSaveTrm());
		}
		Integer joinMin = Collections.min(saveList);
		Integer joinMax = Collections.max(saveList);

		return new FinanceDto(
			deposit.getDepositId(),
			"DEPOSIT",
			deposit.getFinPrdtNm(),
			deposit.getKorCoNm(),
			convertImageUrl(deposit.getBank().getImageName()),
			deposit.getIntrRateShow(),
			deposit.getIntrRate2Show(),
			joinMin,
			joinMax,
			FinancePreferenceDto.toDtoListFromDepositPreference(deposit.getDepositPreferences())
		);
	}

	public static FinanceDto fromSaving(Saving saving) {
		//저축 최소 최대 기간 구함
		List<SavingOption> savingOptions = saving.getSavingOptions();
		List<Integer> saveList = new ArrayList<>();
		for (SavingOption savingOption : savingOptions) {
			saveList.add(savingOption.getSaveTrm());
		}
		Integer joinMin = Collections.min(saveList);
		Integer joinMax = Collections.max(saveList);

		return new FinanceDto(
			saving.getSavingId(),
			"SAVING",
			saving.getFinPrdtNm(),
			saving.getKorCoNm(),
			convertImageUrl(saving.getBank().getImageName()),
			saving.getIntrRateShow(),
			saving.getIntrRate2Show(),
			joinMin,
			joinMax,
			FinancePreferenceDto.toDtoListFromSavingPreference(saving.getSavingPreferences())
		);
	}

}
