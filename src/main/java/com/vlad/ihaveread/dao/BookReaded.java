package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class BookReaded {
    Integer id;
    Integer bookId;
    String dateRead;
    String langRead;
    String titleRead;
    String medium;
    String note;
    Integer score;
}
