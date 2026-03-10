package com.codeheadsystems.hofmann.example.recovery;

import com.codeheadsystems.hofmann.server.recovery.RecoveryChallenger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo recovery challenger that generates 6-digit codes and logs them to the console.
 * In a real application, you would send these via email, SMS, or another out-of-band channel.
 */
public class DemoRecoveryChallenger implements RecoveryChallenger {

  private static final Logger log = LoggerFactory.getLogger(DemoRecoveryChallenger.class);

  private final SecureRandom random = new SecureRandom();
  private final ConcurrentHashMap<String, String> pendingChallenges = new ConcurrentHashMap<>();

  @Override
  public void sendChallenge(byte[] credentialIdentifier) {
    String code = String.format("%06d", random.nextInt(1_000_000));
    String credId = new String(credentialIdentifier, StandardCharsets.UTF_8);
    pendingChallenges.put(credId, code);
    log.info("╔══════════════════════════════════════════╗");
    log.info("║  RECOVERY CODE for {}: {}  ║", credId, code);
    log.info("╚══════════════════════════════════════════╝");
  }

  @Override
  public boolean verifyResponse(byte[] credentialIdentifier, String challengeResponse) {
    String credId = new String(credentialIdentifier, StandardCharsets.UTF_8);
    String expected = pendingChallenges.remove(credId);
    if (expected == null) {
      return false;
    }
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        challengeResponse.getBytes(StandardCharsets.UTF_8));
  }
}
