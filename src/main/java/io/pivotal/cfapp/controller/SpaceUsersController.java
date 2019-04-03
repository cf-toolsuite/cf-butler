package io.pivotal.cfapp.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Mono;

@RestController
public class SpaceUsersController {

	private final SpaceUsersService service;

	@Autowired
	public SpaceUsersController(
		SpaceUsersService service) {
		this.service = service;
	}

	@GetMapping("/space-users")
	public Mono<ResponseEntity<List<SpaceUsers>>> getAllUsers() {
		return service
					.findAll()
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
		return service.obtainUniqueUsernames()
						.map(names -> ResponseEntity.ok(names))
						.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
