package Controller;

import Services.RestaurantRegisterService;
import Services.UserService;
import dao.ItemDao;
import dao.MenuDao;
import dao.RestaurantDao;

import dto.RestaurantDto;
import exception.AlreadyExistValueException;
import exception.ConflictException;
import exception.InvalidInputException;

import exception.NotFoundException;
import model.*;
import observers.RestaurantObserver;

//user dao is now not used anymore, instead we use dedicated daos for different user types

public class RestaurantController {
    private static RestaurantRegisterService restaurantRegisterService;
    private static RestaurantDao restaurantDao;
    private static UserService userService;
    private static MenuDao menuDao=new MenuDao();
    private static ItemDao itemDao=new ItemDao();
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
         restaurantDao.update(res);
         return new RestaurantDto.RegisterReponseRestaurantDto(res.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee());

     }

     public void addMenoToRestaurant (Restaurant restaurant,String title) throws Exception {
        Menu newMenu = new Menu(restaurant,title);
        for (Menu menu:restaurant.getMenus()) {
            if (menu.getTitle().equals(title)) {
                throw new ConflictException(409);
            }
        }
        menuDao.save(newMenu);
        restaurant.addMenu(newMenu);
        restaurantDao.update(restaurant);
     }

     public void deleteMenoFromRestaurant (Restaurant restaurant,String title) throws Exception {
        Menu currentMenu =null;
        for (Menu menu:restaurant.getMenus()) {
             if (menu.getTitle().equals(title)) {
                 currentMenu = menu;
                 restaurant.removeMenu(title);
                 menuDao.delete(currentMenu);
                 restaurantDao.update(restaurant);
             }
         }
        if (currentMenu==null){ throw new NotFoundException(404,"Menu"); }
     }

     public RestaurantDto.AddItemToRestaurantResponseDto addItemTORestaurant(RestaurantDto.AddItemToRestaurantDto itemDto,Restaurant restaurant) throws Exception {
         if (itemDto.name()==null) {throw new InvalidInputException(400, "name");}
         if (itemDto.description()==null) {throw new InvalidInputException(400, "description");}
         if (itemDto.price()<0) {throw new InvalidInputException(400, "price");}
         if (itemDto.supply()<=0) {throw new InvalidInputException(400, "supply");}
         for (String key:itemDto.keywords()) {
             if (key==null) {throw new InvalidInputException(400, "keywords");}
         }
         Menu baseMenu =restaurant.getMenu("base");
         Item newItem=new Item(itemDto.name(),itemDto.description(),itemDto.price(),itemDto.price(),itemDto.keywords(),itemDto.category());
         baseMenu.addItem(newItem);
         menuDao.save(baseMenu);
         itemDao.save(newItem);
         return new RestaurantDto.AddItemToRestaurantResponseDto(newItem.getId(),itemDto.name(),itemDto.imageBase64(),itemDto.description(),restaurant.getId(),itemDto.price(),itemDto.supply(),itemDto.keywords());
     }

    public void addRestaurantObserver(RestaurantObserver o) {
        restaurantRegisterService.registerObserver(o);
    }
}
