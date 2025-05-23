package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.domain.enums.ErhebungsstelleType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Data
@Document(indexName = "#{ 'suggestion' + @environment.getProperty('elasticsearch.index.suffix') }")
@AllArgsConstructor
@NoArgsConstructor
public class CustomSuggest {

    @Id
    String id;

    @Field(type = FieldType.Text)
    String name;

    ErhebungsstelleType erhebungsstelleType;

    @Field(type = FieldType.Text)
    String fkid;

    @CompletionField(analyzer = "standard", searchAnalyzer = "standard", maxInputLength = 255)
    Completion suggest;

}
