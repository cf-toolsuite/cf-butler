package io.pivotal.cfapp.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@ToString
public class SpaceUsers {

	@Id
    private Long pk;
	private String organization;
	private String space;
	private List<String> auditors;
	private List<String> developers;
	private List<String> managers;

	public Set<String> getUsers() {
		Set<String> users = new HashSet<>();
		users.addAll(auditors);
		users.addAll(developers);
		users.addAll(managers);
		return users;
	}

	public Integer getUserCount() {
		return getUsers().size();
	}
}
