package com.zippyboot.infra.es.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "users")
public class UserDocument {

    @Id
    private String id;

    private String username;

    private String email;
}
