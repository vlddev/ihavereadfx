package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class BookLibFile {
    Integer bookNameId;
    String bookName;
    String bookDir;
    String libFile;
}
