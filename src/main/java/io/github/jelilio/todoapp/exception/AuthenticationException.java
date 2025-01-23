package io.github.jelilio.todoapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {
  public static final String AUTH_VERIFY_EMAIL = "auth-verify-email";
  public static final String AUTH_LOGIN_DISABLED = "auth-login-disabled";
  public static final String AUTH_LOGIN_INVALID = "auth-login-invalid";
  public static final String AUTH_BAD_TOKEN = "auth-bad-access";
  public static final String AUTH_OTP_EXPIRED = "auth-otp-expired";
  public static final String AUTH_OTP_INVALID = "auth-otp-invalid";
  public static final String AUTH_LOGIN_ACTIVATED = "auth-login-activated";
  public static final String AUTH_TOKEN_EXPIRED = "auth-token-expired";

  private String code;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, String code) {
    super(message);
    this.code = code;
  }

  public String getCode(){
    return this.code;
  }
}
