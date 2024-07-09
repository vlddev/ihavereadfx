package com.vlad.ihaveread.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Author {
    Integer id;
    String name;
    String lang;
    String note;

    public String getBaseDir(BookName bn) {
        String ret = "";
        if ("uk".equals(getLang())) {
            ret = "/_ukr/"+getName().substring(0,1).toLowerCase()+"/"+getName();
        } else {
            ret = "/"+getName().substring(0,1).toLowerCase()+"/"+getName()+"/"+bn.getLang();
        }
        return ret;
    }

    public String getBaseDir() {
        String ret = "";
        if ("uk".equals(getLang())) {
            ret = "/_ukr/"+getName().substring(0,1).toLowerCase()+"/"+getName();
        } else {
            ret = "/"+getName().substring(0,1).toLowerCase()+"/"+getName();
        }
        return ret;
    }
}
