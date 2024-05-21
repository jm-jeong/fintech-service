package com.fastcampus.fintechservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastcampus.fintechservice.common.api.Api;
import com.fastcampus.fintechservice.dto.request.UserLoginRequest;
import com.fastcampus.fintechservice.dto.request.UserRegisterRequest;
import com.fastcampus.fintechservice.dto.response.UserLoginResponse;
import com.fastcampus.fintechservice.dto.response.UserResponse;
import com.fastcampus.fintechservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

	private final UserService userService;

	@PostMapping("/join")
	public Api<UserResponse> register(
		@Valid
		@RequestBody UserRegisterRequest request
	) {
		var response = userService.join(request);
		return Api.OK(response);
	}

	@PostMapping("/login")
	public Api<UserLoginResponse> login(
		@Valid
		@RequestBody UserLoginRequest request) {
		String token = userService.login(request);
		return Api.OK(new UserLoginResponse(token));
	}

	@GetMapping("/me")
	public Api<UserResponse> me(Authentication authentication) {
		return Api.OK(UserResponse.from(userService.loadUserByEmail(authentication.getName())));
	}

}