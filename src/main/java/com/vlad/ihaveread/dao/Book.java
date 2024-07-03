package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Book {
    Integer id;
    String title;
    String publishDate;
    String lang;
    String note;
}
