package com.chatApp.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table
public class User {
    @TableGenerator(
            name = "yourTableGenerator",
            allocationSize = 1,
            initialValue = 0)
    @Id
    @GeneratedValue(
            strategy=GenerationType.TABLE,
            generator="yourTableGenerator")
    private int id;
    private String username;
    private String password;
    private String email;
}
