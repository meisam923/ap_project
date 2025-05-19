package controller;

import Services.RestaurantRegisterService;
import dao.RestaurantDao;
import dao.UserDao;
import exception.NotAcceptableException;
import model.*;
import observers.RestaurantObserver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RestaurantController {
    private static RestaurantRegisterService restaurantRegisterService;
    private static RestaurantDao restaurantDao;
    private static UserDao userDao;

    public RestaurantController() {
        userDao = new UserDao();
        restaurantDao = new RestaurantDao();
        restaurantRegisterService =  RestaurantRegisterService.getInstance();
    }

    public boolean createRestaurant(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {

        Restaurant new_restaurant = new Restaurant(address, location, phone_number, title, owner, category);
        /*if (!userdao.findbyid()){
            return false;
        }*/
        if (restaurantDao.findByOwnerId(owner.getId()) == null) {
            restaurantDao.save(new_restaurant);
            restaurantRegisterService.requestConfirmation(new_restaurant.getId());
            return true;
        }
        return false;

    }
    public void addItem (String  title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant, @NotNull String type) throws NotAcceptableException {
        /*Item new_item;
        if (type.equals("Drink")) {
            new_item=new Item(title,description,price,count,hashtags,ItemCategory.DRINK) ;
            menu.addItem(new_item);
            return;
        }
        else {
            if (ItemCategory.buildCategory(type)==null) {
                System.out.println("Invalid Category");
                return ;
            }
            new_item=new Item(title,description,price,count,hashtags,ItemCategory.buildCategory(type)) ;
            menu.addItem(new_item);
            return;

        }*/
    }

    public void addRestaurantObserver(RestaurantObserver o) {
        restaurantRegisterService.registerObserver(o);
    }
}
