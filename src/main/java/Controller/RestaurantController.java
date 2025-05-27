package Controller;

import Services.RestaurantRegisterService;
import Services.UserService;
import com.google.gson.Gson;
import dao.RestaurantDao;

import dao.UserDao;
import dto.RestaurantDto;
import exception.AlreadyExistValueException;
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
    private static UserService userService;
    public RestaurantController() {
        restaurantDao = new RestaurantDao();
        restaurantRegisterService =  RestaurantRegisterService.getInstance();
        userService = UserService.getInstance();
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
        if (restaurantDao.findByPhone(restaurant.phone())!=null || userService.findByPhone(restaurant.phone())!=null ) {
            new AlreadyExistValueException(409, "phone");
        }
        Restaurant newRestaurant = new Restaurant(restaurant.address(),restaurant.phone(),restaurant.name(),owner);
        owner.setRestaurant(newRestaurant);
        restaurantDao.save(newRestaurant);
        return new RestaurantDto.RegisterReponseRestaurantDto(newRestaurant.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee());
    }
     public RestaurantDto.RegisterReponseRestaurantDto editRestaurant(RestaurantDto.RegisterRestaurantDto restaurant,Owner owner) throws InvalidInputException {
         if (restaurant.name()== null) {
             throw new InvalidInputException(400, "name");
         }
         if (restaurant.address()== null) {
             throw new InvalidInputException(400, "address");
         }
         if (restaurant.phone()== null || restaurant.phone().length()!=11 ) {
             throw new InvalidInputException(400, "phone");
         }
         if (restaurant.phone().equals(owner.getRestaurant().getPhone_number()) && restaurantDao.findByPhone(restaurant.phone())!=null ) {
             new AlreadyExistValueException(409, "phone");
         }
         Restaurant res=owner.getRestaurant();
         res.setPhone_number(restaurant.phone()); res.setAddress(restaurant.address()); res.setTitle(restaurant.name()); res.setAdditional_fee(restaurant.additional_fee()); res.setTax_fee(restaurant.tax_fee());
         return new RestaurantDto.RegisterReponseRestaurantDto(res.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee());

     }

    public void addRestaurantObserver(RestaurantObserver o) {
        restaurantRegisterService.registerObserver(o);
    }
}
