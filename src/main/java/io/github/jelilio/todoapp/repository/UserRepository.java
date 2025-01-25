package io.github.jelilio.todoapp.repository;

import io.github.jelilio.todoapp.entity.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
  Mono<User> findByUsernameIgnoreCase(String username);

  Mono<Long> countByUsernameIgnoreCase(String username);

  Mono<Long> countById(String userId);

  @Query(value = "{'$and':[ {'email': ?0}, {'activatedDate': { '$ne': null } } ] }", count = true)
  Mono<Long> countByEmailAvailable(String email);

  @Query("{'$and':[ {'email': ?0}, {'activatedDate': null} ] }")
  Mono<User> findByEmailAndNotActivated(String email);

  @Query("{'$or':[ {'username': ?0}, {'email': ?0} ] }")
  Mono<UserDetails> findByUsernameOrEmail(String usernameOrEmail);

  @Query("{'$or':[ {'username': ?0}, {'email': ?0} ] }")
  Mono<User> findUserByUsernameOrEmail(String usernameOrEmail);

  @Query("{'$and':[ {'$or':[ {'username': ?0}, {'email': ?0} ] }, {'activatedDate': { '$ne': null } } ] }")
  Mono<UserDetails> findByUsernameOrEmailAndActivated(String usernameOrEmail);

  @Query("{'$and':[ {'$or':[ {'username': ?0}, {'email': ?0} ] }, {'activatedDate': { '$ne': null } } ] }")
  Mono<User> findUserByUsernameOrEmailAndActivated(String usernameOrEmail);
}
