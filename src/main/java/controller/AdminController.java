package controller;

import dao.RestaurantDao;
import model.Admin;
import model.Restaurant;

import java.util.ArrayList;

public class AdminController {
    RestaurantDao restaurantdao ;
    Admin admin=new Admin();
    public AdminController() {
        this.restaurantdao = new RestaurantDao();
    }

    public void getNotRegisteredRestaurant(){
        ArrayList<Restaurant> restaurants=new ArrayList<>();
        }
        //give this to http handler for client


}
