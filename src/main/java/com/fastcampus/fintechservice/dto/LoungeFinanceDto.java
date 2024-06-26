package com.fastcampus.fintechservice.dto;

import static com.fastcampus.fintechservice.common.ImageConverter.*;

import java.io.IOException;

import com.fastcampus.fintechservice.db.finance.Deposit;
import com.fastcampus.fintechservice.db.finance.DepositOption;
import com.fastcampus.fintechservice.db.finance.Saving;
import com.fastcampus.fintechservice.db.finance.SavingOption;
import com.fastcampus.fintechservice.db.finance.enums.FinProductType;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoungeFinanceDto {

	private String id;//depostId or savingId
	private FinProductType finProductType;//financeProductType
	private String finPrdtNm;//금융상품명

	private String korCoNm;//은행명
	private String bankImageUrl;//은행이미지 url

	private Double intrRateShow;//저축 금리[소수점2자리]
	private Double intrRate2Show;//최고 우대금리[소수점 2자리]

	private Integer joinMin;//가입기간 최솟값
	private Integer joinMax;//가입기간 최댓값

	public static LoungeFinanceDto depositFrom(Deposit deposit) throws IOException {
		Integer min = deposit.getDepositOptions().stream().mapToInt(
			DepositOption::getSaveTrm).min().orElseThrow();
		Integer max = deposit.getDepositOptions().stream().mapToInt(
			DepositOption::getSaveTrm).max().orElseThrow();

		return LoungeFinanceDto.builder()
			.id(deposit.getDepositId())
			.finProductType(FinProductType.DEPOSIT)
			.finPrdtNm(deposit.getFinPrdtNm())
			.korCoNm(deposit.getKorCoNm())
			.bankImageUrl(convertImageUrl(deposit.getBank().getImageName()))
			.intrRateShow(deposit.getIntrRateShow())
			.intrRate2Show(deposit.getIntrRate2Show())
			.joinMin(min)
			.joinMax(max)
			.build();
	}

	public static LoungeFinanceDto savingFrom(Saving saving) throws IOException {
		Integer min = saving.getSavingOptions().stream().mapToInt(
			SavingOption::getSaveTrm).min().orElseThrow();
		Integer max = saving.getSavingOptions().stream().mapToInt(
			SavingOption::getSaveTrm).max().orElseThrow();

		return LoungeFinanceDto.builder()
			.id(saving.getSavingId())
			.finProductType(FinProductType.SAVING)
			.finPrdtNm(saving.getFinPrdtNm())
			.korCoNm(saving.getKorCoNm())
			.bankImageUrl(convertImageUrl(saving.getBank().getImageName()))
			.intrRateShow(saving.getIntrRateShow())
			.intrRate2Show(saving.getIntrRate2Show())
			.joinMin(min)
			.joinMax(max)
			.build();
	}

}
