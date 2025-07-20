package Controller;

import dao.RestaurantDao;
import dto.VendorDto;
import enums.ApprovalStatus;
import enums.OperationalStatus;
import model.Item;
import model.Menu;
import model.Restaurant;
import model.User;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class VendorController {

    private final RestaurantDao restaurantDao = new RestaurantDao();

    public List<VendorDto.RestaurantSchemaDTO> listVendorsForBuyer(VendorDto.VendorListRequestDTO filterDto) {
        String searchTerm = (filterDto != null) ? filterDto.search() : null;
        List<String> keywords = (filterDto != null) ? filterDto.keywords() : null;
        Double minRating = (filterDto != null) ? filterDto.minRating() : null;

        List<Restaurant> restaurantEntities = restaurantDao.findVendors(
                searchTerm,
                keywords,
                minRating,
                ApprovalStatus.REGISTERED,
                OperationalStatus.OPEN
        );

        if (restaurantEntities == null || restaurantEntities.isEmpty()) {
                return Collections.emptyList();
        }

        return restaurantEntities.stream()
                .map(this::mapRestaurantToSchemaDTO)
                .collect(Collectors.toList());
    }


    public VendorDto.VendorMenuResponseDTO getVendorMenuForBuyer(Long vendorId) throws Exception {
        Restaurant restaurant = restaurantDao.findById(vendorId);
        System.out.println("we are here" + restaurant.getId());
        if (restaurant == null || restaurant.getApprovalStatus() != ApprovalStatus.REGISTERED) {
            return null;
        }


        return buildMenuResponseDTO(restaurant);
    }

    private VendorDto.RestaurantSchemaDTO mapRestaurantToSchemaDTO(Restaurant restaurant) {
        if (restaurant == null) return null;

        boolean isOpen = restaurant.getOperationalStatus() == OperationalStatus.OPEN;

        String category = restaurant.getCategory() != null ? restaurant.getCategory().name() : "Uncategorized";

        return new VendorDto.RestaurantSchemaDTO(
                restaurant.getId(),
                restaurant.getTitle(),
                restaurant.getAddress(),
                category,
                restaurant.getAverageRating(),
                restaurant.getLogoBase64(),
                isOpen,
                restaurant.getTaxFee(),
                restaurant.getAdditionalFee(),
                restaurant.getPhoneNumber()
        );
    }

    private VendorDto.VendorMenuResponseDTO buildMenuResponseDTO(Restaurant restaurant) {
        List<String> menusTitle =new ArrayList<>();
        HashMap<String, List<VendorDto.FoodItemSchemaDTO>> mapMenuToItems = new HashMap<>();
        for (Menu menu : restaurant.getMenus()) {
            menusTitle.add(menu.getTitle());
            List<VendorDto.FoodItemSchemaDTO> items = new ArrayList<>();
            for (Item item : menu.getItems()) {
                items.add(new VendorDto.FoodItemSchemaDTO(item.getId(),item.getTitle(),item.getImageBase64(),item.getDescription(),menu.getRestaurant().getId(),item.getPrice().getPrice(),item.getCount(),item.getHashtags()));
            }
            mapMenuToItems.put(menu.getTitle(),items);
        }
        return new VendorDto.VendorMenuResponseDTO(mapRestaurantToSchemaDTO(restaurant), menusTitle, mapMenuToItems);
    }
}