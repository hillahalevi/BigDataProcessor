package com.zoomInfo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry {

    private String id;
    private String firstName;
    private String lastName;
    private int age;
}
