package com.fastcampus.fintechservice.service;

import com.fastcampus.fintechservice.common.error.LoungeErrorCode;
import com.fastcampus.fintechservice.common.error.UserErrorCode;
import com.fastcampus.fintechservice.common.exception.ApiException;
import com.fastcampus.fintechservice.db.finance.Deposit;
import com.fastcampus.fintechservice.db.finance.DepositRepository;
import com.fastcampus.fintechservice.db.finance.Saving;
import com.fastcampus.fintechservice.db.finance.SavingRepository;
import com.fastcampus.fintechservice.db.finance.enums.FinProductType;
import com.fastcampus.fintechservice.db.liked.Liked;
import com.fastcampus.fintechservice.db.liked.LikedRepository;
import com.fastcampus.fintechservice.db.lounge.Comment;
import com.fastcampus.fintechservice.db.lounge.Lounge;
import com.fastcampus.fintechservice.db.lounge.LoungeQueryRepositoryImpl;
import com.fastcampus.fintechservice.db.lounge.LoungeRepository;
import com.fastcampus.fintechservice.db.user.UserAccount;
import com.fastcampus.fintechservice.dto.LoungeFinanceDto;
import com.fastcampus.fintechservice.dto.UserDto;
import com.fastcampus.fintechservice.dto.request.LoungeRequest;
import com.fastcampus.fintechservice.dto.request.LoungeUpdateRequest;
import com.fastcampus.fintechservice.dto.response.LoungeResponse;
import com.fastcampus.fintechservice.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    private final LoungeQueryRepositoryImpl loungeQueryRepository;


    // 라운지 글 생성
    @Transactional
    public LoungeResponse registerPost(LoungeRequest loungeRequest, UserDto userDto) throws IOException {
        List<Liked> findLiked = likedRepository.findAllByUser(userDto.toEntity());
        // 내가 찜하기한 상품이 있는지 확인
        if (findLiked.isEmpty()) {
            throw new ApiException(LoungeErrorCode.LIKED_PRODUCT_NOT_FOUND, String.format("findLiked is %s", findLiked));
        }
        // 내가 찜하기한 상품인지 확인
        String finProductId1 = loungeRequest.getFinancialProduct1();
        String finProductId2 = loungeRequest.getFinancialProduct2();
        FinProductType finProductType = loungeRequest.getFinProductType();

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
        // 존재하지 않는 상품이 추가되었다면 에러처리
        if (!isFinProductId1Liked || !isFinProductId2Liked) {
            throw new ApiException(
                    LoungeErrorCode.LIKED_PRODUCT_NOT_FOUND,
                    String.format("finProductIdLiked is %s %s", isFinProductId1Liked, isFinProductId2Liked));
        }else if (finProductId1.equals(finProductId2)) {
            throw new ApiException(
                    LoungeErrorCode.LIKED_PRODUCT_DUPLICATE,
                    String.format("finProductId1 is %s, finProductId2 is %s", finProductId1, finProductId2));
        }


        return responseValidateFinProductType(
                loungeRepository.save(requestFromDto(loungeRequest, userDto)));
    }

    // request builder
    private Lounge requestFromDto(LoungeRequest loungeRequestDto, UserDto userDto) {

        UserAccount userAccount = userDto.toEntity();
        return Lounge.builder()
                .user(userAccount)
                .title(loungeRequestDto.getTitle())
                .content(loungeRequestDto.getContent())
                .financialProduct1(loungeRequestDto.getFinancialProduct1())
                .financialProduct2(loungeRequestDto.getFinancialProduct2())
                .financialProduct1Name(loungeRequestDto.getFinProductType() == FinProductType.DEPOSIT ?
                        depositRepository.findById(loungeRequestDto.getFinancialProduct1()).get().getFinPrdtNm() :
                        savingRepository.findById(loungeRequestDto.getFinancialProduct1()).get().getFinPrdtNm())
                .financialProduct2Name(loungeRequestDto.getFinProductType() == FinProductType.DEPOSIT ?
                        depositRepository.findById(loungeRequestDto.getFinancialProduct2()).get().getFinPrdtNm() :
                        savingRepository.findById(loungeRequestDto.getFinancialProduct2()).get().getFinPrdtNm())
                .finProductType(loungeRequestDto.getFinProductType())
                .build();
    }

    // 라운지 글 가져오기

    @Transactional
    public LoungeResponse getPost(Long postId) throws IOException {

        Lounge lounge = validatePost(postId);
        viewPost(lounge.getUser(), postId, lounge);

        return responseValidateFinProductType(lounge);
    }


    // 카테고리 전체조회
    @Transactional
    public Page<LoungeResponse> getAllCategoryPosts(FinProductType finProductType, Pageable pageable) {

        Page<LoungeResponse> loungeList = loungeQueryRepository.findAllByFinProductType(finProductType,pageable);
        if(loungeList.isEmpty()) {
            throw new ApiException(LoungeErrorCode.LOUNGE_POST_NOT_FOUND, "게시글이 없습니다.");
        }
        return loungeList;
    }

    // 전체 조회
    @Transactional
    public Page<LoungeResponse> getAllPosts(Pageable pageable) {

        Page<LoungeResponse> loungeList = loungeQueryRepository.findAllPosts(pageable);
        if(loungeList.isEmpty()) {
            throw new ApiException(LoungeErrorCode.LOUNGE_POST_NOT_FOUND, "게시글이 없습니다.");
        }
        return loungeList;
    }
    // 전체 검색
    @Transactional
    public Page<LoungeResponse> searchPosts(String keyword, Pageable pageable) {
        Page<LoungeResponse> loungeList = loungeQueryRepository.searchPosts(keyword, pageable);
        if(loungeList.isEmpty()) {
            throw new ApiException(LoungeErrorCode.LOUNGE_SEARCH_RESULT_NOT_FOUND, "해당 키워드로 검색된 게시글이 없습니다.");
        }
        return loungeList;
    }

    // 라운지 글 업데이트(글제목, 글내용만 수정)
    @Transactional
    public LoungeResponse updatePost(Long postId, LoungeUpdateRequest loungeUpdateRequest) throws IOException {
        Lounge lounge = validatePost(postId);
        validateUser(lounge, lounge.getUser());
        lounge.loungeUpdate(loungeUpdateRequest);
        return responseValidateFinProductType(lounge);
    }



    // 라운지 글 삭제

    @Transactional
    public MessageResponse deletePost(Long postId) {
        Lounge lounge = validatePost(postId);
        validateUser(lounge, lounge.getUser());
        loungeRepository.delete(lounge);
        return new MessageResponse("삭제 완료");
    }


    // 라운지 글 유효성체크
    public Lounge validatePost(Long postId) {
        return loungeRepository.findById(postId)
                .orElseThrow(() -> new ApiException(LoungeErrorCode.LOUNGE_POST_NOT_FOUND,
                        String.format("postId is %s", postId)));
    }
    // 예금 유효성 체크
    public Deposit validateDeposit(String depositId) {
        return depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(LoungeErrorCode.DEPOSIT_NOT_FOUND,
                        String.format("depositId is %s", depositId)));
    }
    // 저축 유효성 체크
    public Saving validateSaving(String savingId) {
        return savingRepository.findById(savingId)
                .orElseThrow(() -> new ApiException(LoungeErrorCode.SAVING_NOT_FOUND,
                        String.format("savingId is %s", savingId)));
    }
    // 금융상품 타입 확인 후 데이터 매핑
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

    private void validateUser(Lounge lounge, UserAccount user) {
        if (!lounge.getUser().equals(user)) {
            throw new ApiException(UserErrorCode.USER_NOT_FOUND, String.format("User not found. userId: %d", user.getId()));
        }
    }



    // 중복 조회수 방지
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




