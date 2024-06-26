package com.fastcampus.fintechservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FinanceDetailRequest {

	@NotNull
	@NotBlank
	String id;

	@NotNull
	@NotBlank
	String type;

}
