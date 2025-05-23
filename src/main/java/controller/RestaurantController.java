package controller;

import Services.RestaurantRegisterService;
import com.google.gson.Gson;
import dao.RestaurantDao;
import dao.UserDao;
import exception.InvalidInputException;
import exception.NotAcceptableException;
import model.*;
import observers.RestaurantObserver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RestaurantController {
    private static RestaurantRegisterService restaurantRegisterService;
    private static RestaurantDao restaurantDao;
    private static UserDao userDao;

    public RestaurantController() {
        userDao = new UserDao();
        restaurantDao = new RestaurantDao();
        restaurantRegisterService =  RestaurantRegisterService.getInstance();
    }

    public String createRestaurant(Restaurant restaurant) throws  InvalidInputException {
        if (restaurant.getTitle() == null) {
            throw new InvalidInputException(400, "name");
        }
        if (restaurant.getAddress().getAddressDetails() == null) {
            throw new InvalidInputException(400, "address");
        }
        if (restaurant.getPhone_number()== null || restaurant.getPhone_number().length()!=10) {
            throw new InvalidInputException(400, "phone");
        }
        restaurant.setOwner(new Owner());
        restaurantDao.save(restaurant);
        return new Gson().toJson(restaurant);

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
