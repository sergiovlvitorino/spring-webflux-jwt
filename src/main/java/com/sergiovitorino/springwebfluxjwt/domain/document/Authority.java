package com.sergiovitorino.springwebfluxjwt.domain.document;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Authority implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

}
