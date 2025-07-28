package Controller;

import Services.RestaurantRegisterService;
import Services.UserService;
import dao.*;

import dto.RestaurantDto;
import enums.OrderRestaurantStatus;
import enums.OrderStatus;
import exception.*;

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
    private static OrderDao orderDao = new OrderDao();
    private static ReviewDao reviewDao = new ReviewDao();


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
        restaurantDao.save(newRestaurant);
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

    public void addMenoToRestaurant (int restaurantID,String title) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long) restaurantID);
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

    public void deleteMenoFromRestaurant (int restaurantID,String title) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long)restaurantID);
        Menu currentMenu = restaurant.getMenu(title);
        if (currentMenu == null) {
            throw new NotFoundException(404, "Menu");
        }
        if (currentMenu.getTitle().equals("Base")) {
            throw new ForbiddenException(403);
        }
        restaurant.removeMenu(title);

        menuDao.deleteById(currentMenu.getId());

        restaurantDao.update(restaurant);
    }

    public RestaurantDto.AddItemToRestaurantResponseDto addItemTORestaurant(RestaurantDto.AddItemToRestaurantDto itemDto,int restaurantID) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long)restaurantID);
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
        newItem.setRestaurant(restaurant);
        itemDao.save(newItem);
        baseMenu.addItem(newItem);
        newItem.addToMenu(baseMenu);
        itemDao.update(newItem); menuDao.update(baseMenu);
        return new RestaurantDto.AddItemToRestaurantResponseDto(newItem.getId(),itemDto.name(),itemDto.imageBase64(),itemDto.description(),restaurant.getId(),itemDto.price(),itemDto.supply(),itemDto.keywords());
    }

    public RestaurantDto.AddItemToRestaurantResponseDto editItemTORestaurant(RestaurantDto.AddItemToRestaurantDto itemDto,int restaurantID,int itemID) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long)restaurantID);
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

    public void deleteItemfromRestaurant(int restaurantID,int itemId) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Restaurant restaurant = em.find(Restaurant.class, (long) restaurantID);
            Item item = em.find(Item.class, itemId);

            if (item == null) throw new NotFoundException(404, "Item");
            if (restaurant.getId() != item.getRestaurant().getId())
                throw new NotFoundException(404, "Item not found in this restaurant");

            // Remove from menus (both sides)
            for (Menu menu : new ArrayList<>(item.getMenus())) {
                menu.getItems().remove(item);
                item.getMenus().remove(menu);
                em.merge(menu); // ensure owning side is updated
            }

            em.merge(item); // sync inverse side
            em.remove(item); // now safe to delete

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete item", e);
        } finally {
            em.close();
        }
    }
    public void addAItemToMenu(int restaurantID,String title,int itemID) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long)restaurantID);
        if (restaurant.getMenu(title) == null) {
            throw new NotFoundException(404, "Menu");
        }
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
    public void deleteAItemFromMenu(int restaurantID,String title,int itemID) throws Exception {
        Restaurant restaurant =restaurantDao.findById((long)restaurantID);
        if (restaurant.getMenu(title) == null) {
            throw new NotFoundException(404, "Menu");
        }
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
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpqlString = new StringBuilder("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("restaurantId", restaurantId);

            if (queryFilters != null) {
                if (queryFilters.containsKey("status") && !queryFilters.get("status").isEmpty()) {
                    jpqlString.append(" AND o.restaurantStatus = :restaurantStatus");
                    parameters.put("restaurantStatus", OrderRestaurantStatus.fromString(queryFilters.get("status")));
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
                }            }

            TypedQuery<Order> orderQuery = em.createQuery(jpqlString.toString(), Order.class);
            parameters.forEach(orderQuery::setParameter);
            List<Order> orders = orderQuery.getResultList();

            if (orders.isEmpty()) {
                return new ArrayList<>();
            }
            List<Long> orderIds = orders.stream().map(Order::getId).collect(java.util.stream.Collectors.toList());

            List<Review> reviews = em.createQuery(
                            "SELECT r FROM Review r LEFT JOIN FETCH r.imagesBase64 WHERE r.order.id IN :orderIds", Review.class)
                    .setParameter("orderIds", orderIds)
                    .getResultList();

            Map<Long, Review> reviewMap = reviews.stream()
                    .collect(java.util.stream.Collectors.toMap(review -> review.getOrder().getId(), review -> review));


            List<RestaurantDto.OrderResponseDto> orderResponseDtos = new ArrayList<>();
            for (Order order : orders) {
                Review review = reviewMap.get(order.getId());

                RestaurantDto.ReviewDto reviewDto = null;
                if (review != null) {
                    reviewDto = new RestaurantDto.ReviewDto(
                            review.getId(),
                            review.getRating(),
                            review.getComment(),
                            review.getReply(),
                            review.getImagesBase64(),
                            review.getCreatedAt().toString()
                    );
                }

                List<RestaurantDto.OrderItemDto> items = new ArrayList<>();
                order.getItems().forEach(item -> items.add(new RestaurantDto.OrderItemDto(item.getItemId(), item.getItemName(), item.getPricePerItem(), item.getTotalPriceForItem(), item.getQuantity())));

                orderResponseDtos.add(new RestaurantDto.OrderResponseDto(
                        order.getId().intValue(),
                        order.getDeliveryAddress(),
                        order.getCustomer() != null ? order.getCustomer().getId().intValue() : null,
                        order.getRestaurant() != null ? order.getRestaurant().getId() : null,
                        order.getCoupon() != null ? order.getCoupon().getId() : null,
                        items,
                        order.getSubtotalPrice(),
                        order.getTaxFee(),
                        order.getAdditionalFee(),
                        order.getDeliveryFee(),
                        order.getTotalPrice(),
                        order.getDeliveryman() != null ? order.getDeliveryman().getId().intValue() : null,
                        order.getStatus() != null ? order.getStatus().name() : null,
                        order.getCreatedAt() != null ? order.getCreatedAt().toString() : null,
                        order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null,
                        order.getRestaurantStatus().name(),
                        reviewDto
                ));
            }

            return orderResponseDtos;

        } finally {
            if (em != null) em.close();
        }
    }
    public void submitReply(String reviewID , String reply,Owner owner) throws Exception {
        Long ID = Long.parseLong(reviewID);
        Review review = reviewDao.findById(ID);
        if (review.getOrder().getRestaurant().getId() != owner.getRestaurant().getId() ) {
            throw new ForbiddenException(403);
        }
        if (review == null) {
            throw new NotFoundException(404, "Review not found");
        }
        if (review.getReply() != null || !review.getReply().isEmpty() || review.getComment() == null) {
            throw new ForbiddenException(403);
        }
        review.setReply(reply);
        reviewDao.update(review);
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
    public void changeOrderStatus(Owner owner, String status, long orderId) throws Exception {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new NotFoundException(404, "Resource Not Found");
        }
        if (owner.getRestaurant().getId() != order.getRestaurant().getId()) {
            throw new NotFoundException(404, "Resource Not Found");
        }
        OrderRestaurantStatus orderStatusEnum = OrderRestaurantStatus.fromString(status);
        if (orderStatusEnum == order.getRestaurantStatus()) {
            throw new ConflictException(409);
        }

        order.setRestaurantStatus(orderStatusEnum);

        // --- THIS IS THE FIX ---
        // The logic from the old updateStatus() method is now here, where it's clear and explicit.
        switch (orderStatusEnum) {
            case ACCEPTED:
                order.setStatus(OrderStatus.WAITING_VENDOR);
                break;
            case REJECTED:
                order.setStatus(OrderStatus.CANCELLED);
                // Return the items to stock if the order is rejected
                for (OrderItem item : order.getItems()) {
                    Item restaurantItem = itemDao.findById(item.getItemId());
                    if (restaurantItem != null) {
                        restaurantItem.increaseCount(item.getQuantity());
                        itemDao.save(restaurantItem);
                    }
                }
                break;
            case SERVED:
                order.setStatus(OrderStatus.FINDING_COURIER);
                break;
        }
        // --- END OF FIX ---

        orderDao.update(order);
    }
}
