package io.pivotal.cfapp.controller;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class SpaceUsersController {

	private final SpaceUsersService service;
	private final EmailValidator validator;

	@Autowired
	public SpaceUsersController(
		SpaceUsersService service) {
		this.service = service;
		this.validator = new EmailValidator();
	}

	@GetMapping(value = { "/space-users" })
	public Mono<ResponseEntity<List<SpaceUsers>>> getAllUsers() {
		return service
					.findAll()
						.collectList()
							.map(users -> ResponseEntity.ok(users))
							.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping(value = { "/snapshot/detail/users" }, produces = MediaType.TEXT_PLAIN_VALUE)
	public Mono<ResponseEntity<List<String>>> getUserList() {
		return service
					.findAll()
						.map(su -> 
							String.join(",", su.getOrganization(), su.getSpace(), su.getUsers().stream().filter(u -> validator.validate(u)).collect(Collectors.joining(";"))))
						.filter(s -> !s.endsWith(","))
							.collectList()
							.map(users -> ResponseEntity.ok(users))
							.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/space-users/{organization}/{space}")
	public Mono<ResponseEntity<SpaceUsers>> getUsersInOrganizationAndSpace(
		@PathVariable("organization") String organization,
		@PathVariable("space") String space) {
		return service
					.findByOrganizationAndSpace(organization, space)
					.map(users -> ResponseEntity.ok(users))
					.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/users/count")
	public Mono<ResponseEntity<Integer>> getUserCount() {
		return service.count()
						.map(c -> ResponseEntity.ok(c));
	}

	@GetMapping("/users")
	public Mono<ResponseEntity<Set<String>>> getUniqueUsernames() {
		Flux<String> users = 
			service
				.obtainUniqueUsernames()
				.flux()
				.flatMap(n -> Flux.fromIterable(n))
				.filter(m -> validator.validate(m));

		Flux<String> serviceAccounts = 
			service
				.obtainUniqueUsernames()
				.flux()
				.flatMap(n -> Flux.fromIterable(n))
				.filter(m -> !validator.validate(m));

		return Flux.concat(users, serviceAccounts)
						.collect(Collectors.toSet())
						.map(o -> ResponseEntity.ok(o))
						.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	public class EmailValidator {

		private final Pattern pattern;
	
		private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
		public EmailValidator() {
			pattern = Pattern.compile(EMAIL_PATTERN);
		}

		public boolean validate(final String hex) {
			Matcher matcher = pattern.matcher(hex);
			return matcher.matches();
		}
	}
}
