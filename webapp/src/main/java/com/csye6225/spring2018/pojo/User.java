package com.csye6225.spring2018.pojo;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "user_table")

public class User implements Serializable{


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "userID", unique=true, nullable = false)
    int id;

    @Column(name = "username")
    String username;

    @Column(name = "password")
    String password;

    @Column(name = "bio")
    String bio;

    @Column(name = "imageName")
    String imageName;
//
//    @Transient
//    MultipartFile images;
//
//    public MultipartFile getImages() {
//        return images;
//    }
//
//    public void setImages(MultipartFile images) {
//        this.images = images;
//    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }


}
