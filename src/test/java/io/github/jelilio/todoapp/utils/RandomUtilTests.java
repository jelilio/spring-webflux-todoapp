package io.github.jelilio.todoapp.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RandomUtilTests {
  @Autowired
  RandomUtil randomUtil;

  private static final int FOUR = 4;

  @Test
  public void checkIfGenerateOtpIsFourInLength() {
    var value = randomUtil.generateOtp();
    assert value.length() == FOUR;
  }
}
