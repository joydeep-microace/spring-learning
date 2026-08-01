package com.example.spring_learning.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Test {


    @Id
    @GeneratedValue( strategy = GenerationType.UUID )
    private String id;


    private String name;

    private String email;

    private boolean isWorking;

}
