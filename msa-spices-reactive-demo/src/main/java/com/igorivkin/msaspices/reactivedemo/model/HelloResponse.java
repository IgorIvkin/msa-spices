package com.igorivkin.msaspices.reactivedemo.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelloResponse {

    private String userId;

    private String name;
}
