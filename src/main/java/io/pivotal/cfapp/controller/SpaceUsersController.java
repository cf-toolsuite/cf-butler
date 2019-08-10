package io.pivotal.cfapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import io.pivotal.cfapp.service.TkService;
import io.pivotal.cfapp.service.TkServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class SpaceUsersController {

	private final SpaceUsersService service;
	private final TkServiceUtil util;

	@Autowired
	public SpaceUsersController(
		SpaceUsersService service,
		TkService tkService) {
		this.service = service;
		this.util = new TkServiceUtil(tkService);
	}

	@GetMapping(value = { "/snapshot/spaces/users/{name}" })
	public Mono<ResponseEntity<List<SpaceUsers>>> getSpacesForAccountName(@PathVariable("name") String name) {
		return util.getHeaders()
				.flatMap(h -> service
								.findByAccountName(name)
								.collectList()
								.map(users -> new ResponseEntity<>(users, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = { "/snapshot/spaces/users" })
	public Mono<ResponseEntity<List<SpaceUsers>>> getAllSpaceUsers() {
		return util.getHeaders()
				.flatMap(h -> service
								.findAll()
								.collectList()
								.map(users -> new ResponseEntity<>(users, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/snapshot/{organization}/{space}/users")
	public Mono<ResponseEntity<SpaceUsers>> getUsersInOrganizationAndSpace(
		@PathVariable("organization") String organization,
		@PathVariable("space") String space) {
		return util.getHeaders()
				.flatMap(h -> service
								.findByOrganizationAndSpace(organization, space)
								.map(users -> new ResponseEntity<>(users, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/snapshot/users/count")
	public Mono<ResponseEntity<Long>> totalAccounts() {
		return util.getHeaders()
				.flatMap(h -> service
								.obtainAccountNames()
								.count()
								.map(c -> new ResponseEntity<>(c, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.ok(0L));
	}

	@GetMapping("/snapshot/users")
	public Mono<ResponseEntity<List<String>>> allAccountNames() {
		return util.getHeaders()
				.flatMap(h -> service.obtainAccountNames()
								.collectList()
								.map(names -> new ResponseEntity<>(names, h, HttpStatus.OK)))
								.defaultIfEmpty(ResponseEntity.notFound().build());
	}

}
