package io.github.jelilio.todoapp.service.impl;

import io.github.jelilio.todoapp.entity.OtpCache;
import io.github.jelilio.todoapp.service.OtpService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class OtpServiceImpl implements OtpService {
  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public OtpServiceImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  public Mono<String> createOtp(String email, String otp, Duration duration) {
    Query query = Query.query(where("_id").is(email));

    return reactiveMongoTemplate.findAndReplace(query, new OtpCache(email, otp, duration))
        .switchIfEmpty(reactiveMongoTemplate.insert(new OtpCache(email, otp, duration)))
        .flatMap(it -> reactiveMongoTemplate.indexOps(OtpCache.class)
            .ensureIndex(new Index().on("expiredDate", Sort.Direction.ASC).expire(0)));
  }

  @Override
  public Mono<Optional<String>> getOtp(String email) {
    Query query = Query.query(where("_id").is(email));

    return reactiveMongoTemplate.findOne(query, OtpCache.class)
        .map(it -> Optional.of(it.getOtpKey()))
        .switchIfEmpty(Mono.just(Optional.empty()));
  }
}
