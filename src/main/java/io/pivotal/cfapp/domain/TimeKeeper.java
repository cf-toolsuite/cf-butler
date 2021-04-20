package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

@Getter
@Table("time_keeper")
public class TimeKeeper {

    @Id
    private LocalDateTime collectionTime;

    @PersistenceConstructor
    public TimeKeeper(LocalDateTime collectionTime) {
        this.collectionTime = collectionTime;
    }
}
