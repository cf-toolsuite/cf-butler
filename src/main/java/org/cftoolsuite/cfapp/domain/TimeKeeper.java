package org.cftoolsuite.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;

@Getter
@Table("time_keeper")
public class TimeKeeper {

    @Id
    private LocalDateTime collectionTime;

    @PersistenceCreator
    public TimeKeeper(LocalDateTime collectionTime) {
        this.collectionTime = collectionTime;
    }
}
