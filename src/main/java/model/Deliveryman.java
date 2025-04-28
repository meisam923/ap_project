package model;

public class Deliveryman extends User{
        private String cordination_location;//مختصات
        public Deliveryman (String first_name,String last_name,String phone_number,String email,String password,String cordination_location) {
            super(first_name, last_name, phone_number, email, password,Role.DELIVERYMAN);
            this.cordination_location = cordination_location;

        }

}
