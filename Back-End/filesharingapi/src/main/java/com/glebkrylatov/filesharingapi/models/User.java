package com.glebkrylatov.filesharingapi.models;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    public String id;
    public String email;
    public String login;
    public String firstName;
    public String lastName;
}