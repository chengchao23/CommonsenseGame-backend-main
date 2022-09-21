package com.chengchao.CommonsenseGame.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor

public class Response<T> {

    private String msg;

    private T data;
}
