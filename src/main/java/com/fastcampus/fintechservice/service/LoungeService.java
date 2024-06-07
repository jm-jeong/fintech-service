package com.fastcampus.fintechservice.service;

import com.fastcampus.fintechservice.common.error.LoungeErrorCode;
import com.fastcampus.fintechservice.common.exception.ApiException;
import com.fastcampus.fintechservice.db.finance.Deposit;
import com.fastcampus.fintechservice.db.finance.DepositRepository;
import com.fastcampus.fintechservice.db.finance.Saving;
import com.fastcampus.fintechservice.db.finance.SavingRepository;
import com.fastcampus.fintechservice.db.finance.enums.FinProductType;
import com.fastcampus.fintechservice.db.liked.Liked;
import com.fastcampus.fintechservice.db.liked.LikedRepository;
import com.fastcampus.fintechservice.db.lounge.Lounge;
import com.fastcampus.fintechservice.db.lounge.LoungeRepository;
import com.fastcampus.fintechservice.db.user.UserAccount;
import com.fastcampus.fintechservice.dto.LoungeFinanceDto;
import com.fastcampus.fintechservice.dto.UserDto;
import com.fastcampus.fintechservice.dto.request.LoungeRequest;
import com.fastcampus.fintechservice.dto.response.LoungeResponse;
import com.fastcampus.fintechservice.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoungeService {

    private final LoungeRepository loungeRepository;
    private final LikedRepository likedRepository;
    private final DepositRepository depositRepository;
    private final SavingRepository savingRepository;
    private final RedisTemplate<String, String> redisTemplateView;



    // 라운지 글 생성
    @Transactional
    public LoungeResponse registerPost(LoungeRequest loungeRequestDto, UserDto userDto) throws IOException {
        List<Liked> findLiked = likedRepository.findAllByUser(userDto.toEntity());
        if (findLiked.isEmpty()) {
            throw new ApiException(LoungeErrorCode.LIKED_PRODUCT_NOT_FOUND, String.format("findLiked is %s", findLiked));
        }

        String finProductId1 = loungeRequestDto.getFinancialProduct1();
        String finProductId2 = loungeRequestDto.getFinancialProduct2();
        FinProductType finProductType = loungeRequestDto.getFinProductType();

        boolean isFinProductId1Liked = findLiked.stream()
                .anyMatch(liked -> {
                    if (finProductType == FinProductType.DEPOSIT) {
                        return liked.getDeposit() != null && liked.getDeposit().getDepositId().equals(finProductId1);
                    } else {
                        return liked.getSaving() != null && liked.getSaving().getSavingId().equals(finProductId1);
                    }
                });

        boolean isFinProductId2Liked = findLiked.stream()
                .anyMatch(liked -> {
                    if (finProductType == FinProductType.DEPOSIT) {
                        return liked.getDeposit() != null && liked.getDeposit().getDepositId().equals(finProductId2);
                    } else {
                        return liked.getSaving() != null && liked.getSaving().getSavingId().equals(finProductId2);
                    }
                });

        if (!isFinProductId1Liked || !isFinProductId2Liked) {
            throw new ApiException(
                    LoungeErrorCode.LIKED_PRODUCT_NOT_FOUND,
                    String.format("finProductIdLiked is %s %s", isFinProductId1Liked, isFinProductId2Liked));
        }

        return responseValidateFinProductType(
                loungeRepository.save(requestFromDto(loungeRequestDto, userDto)));
    }

    // request builder
    private Lounge requestFromDto(LoungeRequest loungeRequestDto, UserDto userDto) {

        UserAccount userAccount = userDto.toEntity();
        return Lounge.builder()
                .userAccount(userAccount)
                .title(loungeRequestDto.getTitle())
                .content(loungeRequestDto.getContent())
                .financialProduct1(loungeRequestDto.getFinancialProduct1())
                .financialProduct2(loungeRequestDto.getFinancialProduct2())
                .finProductType(loungeRequestDto.getFinProductType())
                .build();
    }

    // 라운지 글 가져오기

    @Transactional
    public LoungeResponse getPost(Long postId) throws IOException {

        Lounge lounge = validatePost(postId);
        viewPost(lounge.getUserAccount(), postId, lounge);
        return responseValidateFinProductType(lounge);
    }

    @Transactional
    public Page<LoungeResponse> getAllLounge(FinProductType finProductType, Pageable pageable) throws IOException {
        Page<Lounge> loungesPage;
        int page = pageable.getPageNumber() -1;
        int pageLimit = pageable.getPageSize();


        if (finProductType == null) {
            loungesPage = loungeRepository.findAllByOrderByViewCountDesc(
                    PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "viewCount")));
        } else {
            loungesPage = loungeRepository.findAllByFinProductTypeOrderByViewCountDesc(
                    pageable, finProductType);
        }

        List<LoungeResponse> loungeResponses = new ArrayList<>();
        for (Lounge lounge : loungesPage.getContent()) {
            LoungeResponse loungeResponse = responseValidateFinProductType(lounge);
            loungeResponses.add(loungeResponse);
        }

        return new PageImpl<>(loungeResponses, pageable, loungesPage.getTotalElements());
    }







    // 라운지 글 업데이트
    @Transactional
    public LoungeResponse updatePost (Long postId, LoungeRequest loungeRequest) throws IOException {
        Lounge lounge = validatePost(postId);
        lounge.loungeUpdate(loungeRequest);
        return responseValidateFinProductType(lounge);
    }


    // 라운지 글

    @Transactional
    public MessageResponse deletePost(Long postId) {
        Lounge lounge = validatePost(postId);
        loungeRepository.delete(lounge);
        return new MessageResponse("삭제 완료");
    }


    // 라운지 글 유효성체크
    public Lounge validatePost(Long postId) {
        return loungeRepository.findById(postId)
                .orElseThrow(() -> new ApiException(
                        LoungeErrorCode.LOUNGE_POST_NOT_FOUND,
                        String.format("postId is %s", postId)));
    }

    public Deposit validateDeposit(String depositId) {
        return depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(
                        LoungeErrorCode.DEPOSIT_NOT_FOUND,
                        String.format("depositId is %s", depositId)));
    }

    public Saving validateSaving(String savingId) {
        return savingRepository.findById(savingId)
                .orElseThrow(() -> new ApiException(
                        LoungeErrorCode.SAVING_NOT_FOUND,
                        String.format("savingId is %s", savingId)));
    }

    public LoungeResponse responseValidateFinProductType(Lounge lounge) throws IOException {
        if(lounge.getFinProductType().equals(FinProductType.DEPOSIT)) {
            LoungeFinanceDto loungeFinanceDto1 =
                    LoungeFinanceDto.depositFrom(validateDeposit(lounge.getFinancialProduct1()));
            LoungeFinanceDto loungeFinanceDto2 =
                    LoungeFinanceDto.depositFrom(validateDeposit(lounge.getFinancialProduct2()));
            return LoungeResponse.fromDeposit(lounge, loungeFinanceDto1, loungeFinanceDto2);
        }else {
            LoungeFinanceDto loungeFinanceDto1 =
                    LoungeFinanceDto.savingFrom(validateSaving(lounge.getFinancialProduct1()));
            LoungeFinanceDto loungeFinanceDto2 =
                    LoungeFinanceDto.savingFrom(validateSaving(lounge.getFinancialProduct2()));
            return LoungeResponse.fromSaving(lounge, loungeFinanceDto1, loungeFinanceDto2);
        }
    }


    public void viewPost(UserAccount user, Long postId, Lounge lounge) {
        // 조회시 키 생성
        String key = "post:" + postId + ":user:" + user.getId();

        // Redis에서 키 존재 여부 확인
        if (Boolean.FALSE.equals(redisTemplateView.hasKey(key))) {
            // 키가 존재하지 않으면 조회수 증가 및 키 저장
            redisTemplateView.opsForValue().increment("post:" + postId + ":views");
            redisTemplateView.opsForValue().set(key, "1", 2592000); // 1달간 키 유지
            lounge.viewCount();
        }
    }
}




