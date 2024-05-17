package com.fastcampus.apiserver.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fastcampus.apiserver.dto.TokenDto;
import com.fastcampus.common.error.ErrorCode;
import com.fastcampus.common.exception.ApiException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

	private String accessToken;
	private LocalDateTime accessTokenExpiredAt;
	private String refreshToken;
	private LocalDateTime refreshTokenExpiredAt;

	public static TokenResponse toResponse(
		TokenDto accessToken,
		TokenDto refreshToken
	) {
		Objects.requireNonNull(accessToken, () -> {
			throw new ApiException(ErrorCode.NULL_POINT);
		});
		Objects.requireNonNull(refreshToken, () -> {
			throw new ApiException(ErrorCode.NULL_POINT);
		});

		return TokenResponse.builder()
			.accessToken(accessToken.getToken())
			.accessTokenExpiredAt(accessToken.getExpiredAt())
			.refreshToken(refreshToken.getToken())
			.refreshTokenExpiredAt(refreshToken.getExpiredAt())
			.build();
	}

}
