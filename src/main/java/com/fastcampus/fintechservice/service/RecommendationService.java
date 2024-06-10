package com.fastcampus.fintechservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastcampus.fintechservice.db.finance.DepositRepository;
import com.fastcampus.fintechservice.db.finance.SavingRepository;
import com.fastcampus.fintechservice.dto.RecommendationDto;
import com.fastcampus.fintechservice.dto.request.RecommendationRequest;
import com.fastcampus.fintechservice.dto.response.RecommendationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RecommendationService {
	private final DepositRepository depositRepository;
	private final SavingRepository savingRepository;

	public RecommendationResponse getRecommendationList(RecommendationRequest recommendationRequest,
		int size) {
		// Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "intrRateShow"));
		Integer savingStart = recommendationRequest.getSavingStart();
		Integer savingEnd = recommendationRequest.getSavingEnd();
		Long savingGoal = recommendationRequest.getSavingGoal().longValue();
		String savingType = recommendationRequest.getSavingType();//S, F

		if (savingType.equals("S")) {
			List<RecommendationDto> savingList = savingRepository.findBySaveTrmUpDownAndMaxLimit(savingStart,
					savingEnd, savingGoal, savingType, size)
				.stream()
				.map(RecommendationDto::fromSaving)
				.collect(Collectors.toList());
			return new RecommendationResponse(savingList, new ArrayList<>(),
				savingList);
		} else if (savingType.equals("F")) {
			List<RecommendationDto> depositList = depositRepository.findBySaveTrmUpDownAndMaxLimit(savingStart,
				savingEnd, savingGoal, size).stream().map(RecommendationDto::fromDeposit).collect(Collectors.toList());
			List<RecommendationDto> savingList = savingRepository.findBySaveTrmUpDownAndMaxLimit(savingStart,
					savingEnd, savingGoal, savingType, size)
				.stream()
				.map(RecommendationDto::fromSaving)
				.collect(Collectors.toList());

			ArrayList<RecommendationDto> allList = new ArrayList<>();
			allList.addAll(savingList);
			allList.addAll(depositList);

			// intrRateShow를 기준으로 내림차순으로 정렬
			Collections.sort(allList, Comparator.comparing(RecommendationDto::getIntrRateShow).reversed());
			List<RecommendationDto> allTopList = allList.subList(0, size);
			return new RecommendationResponse(allTopList, depositList,
				savingList);
		}

		return new RecommendationResponse();
	}
}