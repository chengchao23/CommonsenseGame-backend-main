package com.chengchao.CommonsenseGame.Entity;

import lombok.Data;

@Data
public class GroupDetail {
    private String name;
    private String teammate;
    private String role;
    private Integer total = 0;
    private float score = 0;
    private float average = 0;

    private Boolean change;

}
