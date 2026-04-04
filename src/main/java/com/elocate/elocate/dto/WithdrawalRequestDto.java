package com.elocate.elocate.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDto {

    @NotNull @DecimalMin("100.00")
    private BigDecimal amount;

    @NotBlank
    private String accountHolderName;

    @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    private String mobileNumber;

    @NotBlank @Pattern(regexp = "^[0-9]{9,18}$", message = "Invalid account number")
    private String accountNumber;

    @NotBlank
    private String bankName;

    @NotBlank @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String ifscCode;

    private String upiId;   // optional
    private String email;   // optional
}
