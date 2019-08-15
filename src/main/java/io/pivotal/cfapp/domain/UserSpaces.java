package io.pivotal.cfapp.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({ "account-name", "spaces" })
public class UserSpaces {

    @JsonProperty("account-name")
    private String accountName;

    @Default
    @JsonProperty("spaces")
    private List<Space> spaces = new ArrayList<>();


    @JsonCreator
    public UserSpaces(
        @JsonProperty("account-name") String accountName,
        @JsonProperty("spaces") List<Space> spaces)
    {
        this.accountName = accountName;
        this.spaces = spaces;
    }


}