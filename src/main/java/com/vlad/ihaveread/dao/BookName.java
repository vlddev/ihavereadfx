package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
//@ToString
public class BookName {
    Integer id;
    Integer bookId;
    String name;
    String lang;
    String goodreadsId;
    String libFile;
    BookLibFile bookLibFile;

    @Override
    public String toString() {
        return ""+id+"|"+lang+"|"+name;
    }
}
