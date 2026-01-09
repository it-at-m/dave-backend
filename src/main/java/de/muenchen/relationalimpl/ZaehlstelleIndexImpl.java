package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ZaehlstelleIndexImpl implements ZaehlstelleIndex {

    private final ZaehlstelleRepository zaehlstelleRepository;

    private final ZaehlstelleMapper zaehlstelleMapper;

    public ZaehlstelleIndexImpl(final ZaehlstelleRepository zaehlstelleRepository,
            final ZaehlstelleMapper zaehlstelleMapper) {
        this.zaehlstelleRepository = zaehlstelleRepository;
        this.zaehlstelleMapper = zaehlstelleMapper;
    }

    public Zaehlstelle save(Zaehlstelle var1) {
        de.muenchen.dave.domain.analytics.Zaehlstelle zaehlstelleEntity = new de.muenchen.dave.domain.analytics.Zaehlstelle();
        if (var1 == null) {
            return null;
        } else if (var1.getId() != null && !var1.getId().isBlank()) {
            zaehlstelleEntity = zaehlstelleRepository.findById(UUID.fromString(var1.getId()))
                    .orElse(zaehlstelleEntity);
        }
        zaehlstelleEntity = zaehlstelleMapper.elastic2analytics(zaehlstelleEntity, var1);
        zaehlstelleEntity = zaehlstelleRepository.save(zaehlstelleEntity);
        if (var1.getId() == null || var1.getId().isBlank()) {
            var1.setId(zaehlstelleEntity.getId().toString());
            //zaehlstelle.setId(UUID.randomUUID().toString());
        }
        return zaehlstelleMapper.analytics2elastic(zaehlstelleEntity);
    }

}
