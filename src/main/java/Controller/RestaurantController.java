package Controller;

import Services.RestaurantRegisterService;
import Services.UserService;
import dao.ItemDao;
import dao.MenuDao;
import dao.OrderDao;
import dao.OwnerDao;
import dao.RestaurantDao;

import dto.RestaurantDto;
import enums.OrderRestaurantStatus;
import enums.OrderStatus;
import exception.AlreadyExistValueException;
import exception.ConflictException;
import exception.InvalidInputException;

import exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.*;
import observers.RestaurantObserver;
import util.JpaUtil;

import java.util.*;
import java.util.stream.Collectors;

public class RestaurantController {
    private static RestaurantRegisterService restaurantRegisterService;
    private static RestaurantDao restaurantDao;
    private static UserService userService;
    private static MenuDao menuDao=new MenuDao();
    private static ItemDao itemDao=new ItemDao();
    private static OwnerDao ownerDao = new OwnerDao();


    public RestaurantController() {
        restaurantDao = new RestaurantDao();
        restaurantRegisterService =  RestaurantRegisterService.getInstance();
        userService = UserService.getInstance();
    }

    public RestaurantDto.RegisterReponseRestaurantDto createRestaurant(RestaurantDto.RegisterRestaurantDto restaurant,Owner owner) throws  InvalidInputException, Exception {
        if (restaurant.name()== null) {
            throw new InvalidInputException(400, "name");
        }
        if (restaurant.address()== null) {
            throw new InvalidInputException(400, "address");
        }
        if (restaurant.phone()== null || restaurant.phone().length()!=11) {
            throw new InvalidInputException(400, "phone");
        }
        if (restaurantDao.findByPhone(restaurant.phone())!=null || userService.findByPhone(restaurant.phone()).isPresent() ) {
            throw new AlreadyExistValueException(409, "phone");
        }
        if (restaurant.tax_fee() <0 || restaurant.additional_fee() <0){
            throw new InvalidInputException(400, "fee");
        }
        Restaurant newRestaurant = new Restaurant(
                restaurant.name(),
                restaurant.address(),
                restaurant.phone(),
                owner,
                restaurant.tax_fee(),
                restaurant.additional_fee(),
                restaurant.logaBase64()
        );
        owner.setRestaurant(newRestaurant);
        ownerDao.update(owner);
        return new RestaurantDto.RegisterReponseRestaurantDto(newRestaurant.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee(),newRestaurant.getApprovalStatus().name().toUpperCase());
    }
    public RestaurantDto.RegisterReponseRestaurantDto editRestaurant(RestaurantDto.RegisterRestaurantDto restaurant,Owner owner) throws Exception {
        System.out.println(restaurant.phone()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (restaurant.name()== null) {
            throw new InvalidInputException(400, "name");

        }
        if (restaurant.address()== null) {
            throw new InvalidInputException(400, "address");
        }
        if (restaurant.phone()== null || restaurant.phone().length()!=11 ) {
            throw new InvalidInputException(400, "phone");
        }
        if (!restaurant.phone().equals(owner.getRestaurant().getPhoneNumber()) &&
                (restaurantDao.findByPhone(restaurant.phone()) != null ||
                        userService.findByPhone(restaurant.phone()).isPresent())) {
           throw new AlreadyExistValueException(409, "phone");
        }
        if (restaurant.tax_fee() <0 || restaurant.additional_fee() <0){
            throw new InvalidInputException(400, "fee");
        }
        Restaurant res=owner.getRestaurant();
        res.setPhoneNumber(restaurant.phone()); res.setAddress(restaurant.address()); res.setTitle(restaurant.name()); res.setAdditionalFee(restaurant.additional_fee()); res.setTaxFee(restaurant.tax_fee()); res.setLogoBase64(restaurant.logaBase64());
        restaurantDao.update(res);
        return new RestaurantDto.RegisterReponseRestaurantDto(res.getId(),restaurant.name(),restaurant.address(),restaurant.phone(),restaurant.logaBase64(),restaurant.tax_fee(),restaurant.additional_fee(),res.getApprovalStatus().name().toUpperCase());

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
        Menu currentMenu = restaurant.getMenu(title);
        if (currentMenu == null) {
            throw new NotFoundException(404, "Menu");
        }

        restaurant.removeMenu(title);

        menuDao.deleteById(currentMenu.getId());

        restaurantDao.update(restaurant);
    }

    public RestaurantDto.AddItemToRestaurantResponseDto addItemTORestaurant(RestaurantDto.AddItemToRestaurantDto itemDto,Restaurant restaurant) throws Exception {
        if (itemDto.name()==null) {throw new InvalidInputException(400, "name");}
        if (itemDto.description()==null) {throw new InvalidInputException(400, "description");}
        if (itemDto.price()<0) {throw new InvalidInputException(400, "price");}
        if (itemDto.supply()<=0) {throw new InvalidInputException(400, "supply");}
        for (String key:itemDto.keywords()) {
            if (key==null) {throw new InvalidInputException(400, "keywords");}
        }
        Menu baseMenu = restaurant.getMenu("Base");
        if (baseMenu == null) {
            baseMenu = new Menu(restaurant, "Base");
        }
        Item newItem=new Item(itemDto.name(),itemDto.description(),itemDto.price(),itemDto.supply(),itemDto.keywords(),itemDto.imageBase64());
        itemDao.save(newItem);
        baseMenu.addItem(newItem);
        newItem.addToMenu(baseMenu);
        itemDao.update(newItem); menuDao.update(baseMenu);
        return new RestaurantDto.AddItemToRestaurantResponseDto(newItem.getId(),itemDto.name(),itemDto.imageBase64(),itemDto.description(),restaurant.getId(),itemDto.price(),itemDto.supply(),itemDto.keywords());
    }

    public RestaurantDto.AddItemToRestaurantResponseDto editItemTORestaurant(RestaurantDto.AddItemToRestaurantDto itemDto,Restaurant restaurant,int itemID) throws Exception {
        if (itemDto.name() == null) {
            throw new InvalidInputException(400, "name");
        }
        if (itemDto.description() == null) {
            throw new InvalidInputException(400, "description");
        }
        if (itemDto.price() < 0) {
            throw new InvalidInputException(400, "price");
        }
        if (itemDto.supply() <= 0) {
            throw new InvalidInputException(400, "supply");
        }
        for (String key : itemDto.keywords()) {
            if (key == null) {
                throw new InvalidInputException(400, "keywords");
            }
        }
        Item item = itemDao.findById(itemID);
        if (item == null) {
            throw new NotFoundException(404, "Item");
        }
        if (restaurant.getId()!=item.getRestaurant().getId()) {
            throw new NotFoundException(404, "Menu");
        }
        item.setTitle(itemDto.name());
        item.setDescription(itemDto.description());
        item.setPrice(itemDto.price());
        item.setCount(itemDto.supply());
        item.setHashtags(itemDto.keywords());
        item.setImageBase64(itemDto.imageBase64());
        itemDao.update(item);
        for (Menu menu:item.getMenus()) {
            menuDao.update(menu);}
        return new RestaurantDto.AddItemToRestaurantResponseDto(item.getId(), itemDto.name(), itemDto.imageBase64(), itemDto.description(), restaurant.getId(), itemDto.price(), itemDto.supply(), itemDto.keywords());
    }

    public void deleteItemfromRestaurant(Restaurant restaurant,int itemId) throws Exception {
        Item item = itemDao.findById(itemId);
        if (item == null) {throw new NotFoundException(404, "Item");}
        if (restaurant.getId() != item.getRestaurant().getId()) { throw new NotFoundException(404, "Item not found in this restaurant");}

        for (Menu menu:item.getMenus()) {
            menu.removeItem(item.getId());
            menuDao.update(menu);
        }

        itemDao.deleteById(itemId);
    }
    public void addAItemToMenu(Restaurant restaurant,String title,int itemID) throws Exception {
        Menu menu =restaurant.getMenu(title);
        if (menu == null) {throw new NotFoundException(404, "Menu");}
        Item item =itemDao.findById(itemID);
        if (item == null) {throw new NotFoundException(404, "Item");}
        for (Menu menutmp: item.getMenus()){
            if (menutmp.getTitle().equals(title))
                throw new ConflictException(409);
        }
        item.addToMenu(menu);
        menu.addItem(item);
        itemDao.update(item);
        return;
    }
    public void deleteAItemFromMenu(Restaurant restaurant,String title,int itemID) throws Exception {
        Menu menu =restaurant.getMenu(title);
        if (menu == null) {throw new NotFoundException(404, "Menu");}
        Item item =itemDao.findById(itemID);
        if (item == null) {throw new NotFoundException(404, "Item");}
        menu.removeItem(item.getId());
        item.removeFromMenu(menu.getId());
        menuDao.update(menu);
    }
    public void addRestaurantObserver(RestaurantObserver o) {
        restaurantRegisterService.registerObserver(o);
    }

    public List<RestaurantDto.OrderResponseDto> getRestaurantOrders(HashMap<String, String> queryFilters, int restaurantId) throws Exception {

        // Start building the database query
        StringBuilder jpqlString = new StringBuilder("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("restaurantId", restaurantId);

        // Add filters based on query parameters
        if (queryFilters != null) {
            if (queryFilters.containsKey("status") && !queryFilters.get("status").isEmpty()) {
                OrderRestaurantStatus statusEnum = OrderRestaurantStatus.fromString(queryFilters.get("status"));
                jpqlString.append(" AND o.restaurantStatus = :restaurantStatus");
                parameters.put("restaurantStatus", statusEnum);
            }
            if (queryFilters.containsKey("user") && !queryFilters.get("user").isEmpty()) {
                jpqlString.append(" AND LOWER(o.customer.fullName) LIKE LOWER(:customerFullName)");
                parameters.put("customerFullName", "%" + queryFilters.get("user") + "%");
            }
            if (queryFilters.containsKey("courier") && !queryFilters.get("courier").isEmpty()) {
                jpqlString.append(" AND o.deliveryman IS NOT NULL AND LOWER(o.deliveryman.fullName) LIKE LOWER(:deliverymanFullName)");
                parameters.put("deliverymanFullName", "%" + queryFilters.get("courier") + "%");
            }
            if (queryFilters.containsKey("search") && !queryFilters.get("search").isEmpty()) {
                jpqlString.append(" AND EXISTS (SELECT 1 FROM o.items item WHERE LOWER(item.itemName) LIKE LOWER(:itemTitle))");
                parameters.put("itemTitle", "%" + queryFilters.get("search") + "%");
            }
        }

        // Execute the query
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Order> typedQuery = em.createQuery(jpqlString.toString(), Order.class);
            parameters.forEach(typedQuery::setParameter);
            List<Order> orders = typedQuery.getResultList();

            List<RestaurantDto.OrderResponseDto> orderResponseDtos = new ArrayList<>();

            // Map the results to DTOs
            for (Order order : orders) {
                List<RestaurantDto.OrderItemDto> items = new ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    items.add(new RestaurantDto.OrderItemDto(item.getItemId(),item.getItemName(),item.getPricePerItem(),item.getTotalPriceForItem(),item.getQuantity()));
                }
                orderResponseDtos.add(new RestaurantDto.OrderResponseDto(
                        order.getId().intValue(),
                        order.getDeliveryAddress(),
                        (order.getCustomer() != null) ? order.getCustomer().getId().intValue() : null,
                        (order.getRestaurant() != null) ? order.getRestaurant().getId() : null,
                        (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                        items,
                        order.getSubtotalPrice(),
                        order.getTaxFee(),
                        order.getAdditionalFee(),
                        order.getDeliveryFee(),
                        order.getTotalPrice(),
                        (order.getDeliveryman() != null) ? order.getDeliveryman().getId().intValue() : null,
                        (order.getStatus() != null) ? order.getStatus().name() : null,
                        (order.getCreatedAt() != null) ? order.getCreatedAt().toString() : null,
                        (order.getUpdatedAt() != null) ? order.getUpdatedAt().toString() : null,
                        order.getRestaurantStatus().name(),
                        order.getReview()!=null ? new RestaurantDto.ReviewDto(order.getReview().getId(),order.getReview().getRating(),order.getReview().getComment(),order.getReview().getImagesBase64(),order.getReview().getCreatedAt().toString()):null
                ));
            }
            return orderResponseDtos;
        } finally {
            if (em != null) em.close();
        }
    }

    public RestaurantDto.RegisterReponseRestaurantDto mapToRegisterResponseDto(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantDto.RegisterReponseRestaurantDto(
                restaurant.getId(),
                restaurant.getTitle(),
                restaurant.getAddress(),
                restaurant.getPhoneNumber(),
                restaurant.getLogoBase64(),
                restaurant.getTaxFee(),
                restaurant.getAdditionalFee(),
                restaurant.getApprovalStatus().name().toUpperCase()
        );
    }
    public void changeOrderStatus(Owner owner,String status,long orderId) throws Exception {
        OrderDao orderDao = new OrderDao();
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new NotFoundException(404,"Resource Not Found");
        }
        if (owner.getRestaurant().getId() != order.getRestaurant().getId()) {
            throw new NotFoundException(404,"Resource Not Found");
        }
        OrderRestaurantStatus orderStatusEnum = OrderRestaurantStatus.fromString(status);
        if (orderStatusEnum == order.getRestaurantStatus()) {
            throw new ConflictException(409);
        }
        order.setRestaurantStatus(orderStatusEnum);
        order.updateStatus();
        orderDao.update(order);
    }
}
