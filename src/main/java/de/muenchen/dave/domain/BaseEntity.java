package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id", length = 36)
    @Id
    @GeneratedValue(generator = "uuid")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @CreatedDate
    @Column(name = "created_time", nullable = false, columnDefinition = "TIMESTAMP", updatable = false)
    private LocalDateTime createdTime;

    @Version
    @Column(name = "version")
    private Long version;

    public Long getEntityVersion() {
        return version;
    }

}
