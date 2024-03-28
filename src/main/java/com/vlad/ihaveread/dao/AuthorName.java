package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class AuthorName {
    public static final String TYPE_NORM = "norm";
    public static final String TYPE_NATURAL = "nat";

    Integer authorId;
    String name;
    String lang;
    String type;
}
