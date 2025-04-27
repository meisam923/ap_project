package model;

public abstract class User {

    private String first_name;
    private String last_name;
    private String phone_number;
    private String email;
    private String password;
    private Role role;

    public User (String first_name,String last_name,String phone_number,String email,String password,Role role){

        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
        this.role = role;

    }


}
