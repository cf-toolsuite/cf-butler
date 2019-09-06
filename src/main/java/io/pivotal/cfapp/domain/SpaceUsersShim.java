package io.pivotal.cfapp.domain;

import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SpaceUsersShim {

	private Long pk;
	private String organization;
	private String space;
	private String auditors;
	private String developers;
	private String managers;

	public static SpaceUsersShim from(SpaceUsers spaceUsers) {
		return SpaceUsersShim
				.builder()
					.pk(spaceUsers.getPk())
					.organization(spaceUsers.getOrganization())
					.space(spaceUsers.getSpace())
					.auditors(CollectionUtils.isEmpty(spaceUsers.getAuditors()) ? null : String.join(",", spaceUsers.getAuditors()))
					.developers(CollectionUtils.isEmpty(spaceUsers.getDevelopers()) ? null : String.join(",", spaceUsers.getDevelopers()))
					.managers(CollectionUtils.isEmpty(spaceUsers.getManagers()) ? null : String.join(",", spaceUsers.getManagers()))
					.build();
	}
}
