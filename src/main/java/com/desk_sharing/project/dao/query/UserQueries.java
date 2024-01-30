package com.desk_sharing.project.dao.query;

public class UserQueries {
    private UserQueries () {}
    public static final String GET_ALL_USERS = "SELECT * FROM users;";
    public static final String GET_BY_USERNAME = "SELECT * FROM users where username = :username;";
    public static final String GET_BY_ID = "SELECT * FROM users where id = :id;";

    public static final String DELETE = "DELETE FROM users WHERE id = :id;";
    public static final String UPSERT_USER = "INSERT INTO users (username, password, email, name, isActive, isAdmin) VALUES (:username, :password, :email, :name, :isActive, :isAdmin) ON DUPLICATE KEY UPDATE password = :password, email = :email, name = :name, isActive = :isActive, isAdmin = :isAdmin;";

}
