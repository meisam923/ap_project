package Controller;

import Services.RestaurantRegisterService;
import com.google.gson.Gson;
import dao.RestaurantDao;

import dao.UserDao;
import dto.RestaurantDto;
import exception.InvalidInputException;

import exception.NotAcceptableException;
import model.*;
import observers.RestaurantObserver;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//user dao is now not used anymore, instead we use dedicated daos for different user types

public class RestaurantController {
    private static RestaurantRegisterService restaurantRegisterService;
    private static RestaurantDao restaurantDao;

    public RestaurantController() {
        restaurantDao = new RestaurantDao();
        restaurantRegisterService =  RestaurantRegisterService.getInstance();
    }

    public RestaurantDto.RegisterReponseRestaurantDto createRestaurant(RestaurantDto.RegisterRestaurantDto restaurant,Owner owner) throws  InvalidInputException {
        if (restaurant.name()== null) {
            throw new InvalidInputException(400, "name");
        }
        if (restaurant.address()== null) {
            throw new InvalidInputException(400, "address");
        }
        if (restaurant.phone()== null || restaurant.phone().length()!=11) {
            throw new InvalidInputException(400, "phone");
        }
        Restaurant newRestaurant = new Restaurant(restaurant.address(),restaurant.phone(),restaurant.name(),owner);
        owner.setRestaurant(newRestaurant);
        restaurantDao.save(newRestaurant);
        return new RestaurantDto.RegisterReponseRestaurantDto(newRestaurant.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee());
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
