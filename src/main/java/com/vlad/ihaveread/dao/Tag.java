package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tag {
    Integer id;
    String nameEn;
    String nameUk;

    @Override
    public String toString() {
        return nameEn + " | " + nameUk;
    }
}
