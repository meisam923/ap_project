package model;

public class Salesman extends User {
    private String address;//آدرس
    private String cordination_location;//مختصات
    public Salesman (String first_name,String last_name,String phone_number,String email,String password,String address,String cordination_location) {
        super(first_name, last_name, phone_number, email, password,Role.SALESMAN);
        this.address = address;
        this.cordination_location = cordination_location;

    }
}
