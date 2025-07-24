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
        System.out.println("\n[DEBUG] --- Entering getVendorMenuForBuyer ---");
        System.out.println("[DEBUG] Attempting to find restaurant with ID: " + vendorId);

        Restaurant restaurant = restaurantDao.findById(vendorId);

        if (restaurant == null) {
            System.out.println("[DEBUG] CRITICAL: restaurantDao.findById returned NULL for ID: " + vendorId);
            return null; // This will result in a 404 Not Found
        }

        System.out.println("[DEBUG] Restaurant found. ID: " + restaurant.getId() + ", Name: " + restaurant.getTitle());

        if (restaurant.getApprovalStatus() != ApprovalStatus.REGISTERED) {
            System.out.println("[DEBUG] Restaurant is not approved. Status: " + restaurant.getApprovalStatus());
            return null;
        }

        System.out.println("[DEBUG] Restaurant is approved. Proceeding to build menu response...");
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
        System.out.println("[DEBUG] --- Entering buildMenuResponseDTO for restaurant: " + restaurant.getTitle() + " ---");

        List<String> menusTitle = new ArrayList<>();
        HashMap<String, List<VendorDto.FoodItemSchemaDTO>> mapMenuToItems = new HashMap<>();

        if (restaurant.getMenus() == null) {
            System.out.println("[DEBUG] CRITICAL: restaurant.getMenus() returned NULL.");
        } else if (restaurant.getMenus().isEmpty()) {
            System.out.println("[DEBUG] INFO: Restaurant has no menus.");
        } else {
            System.out.println("[DEBUG] Restaurant has " + restaurant.getMenus().size() + " menu(s). Looping through them...");

            for (Menu menu : restaurant.getMenus()) {
                if (menu == null) {
                    System.out.println("[DEBUG] CRITICAL: Found a NULL menu object in the list.");
                    continue; // Skip this null menu
                }

                System.out.println("\n[DEBUG] Processing Menu -> Title: '" + menu.getTitle() + "', ID: " + menu.getId());

                // This is a crucial check
                if (menu.getRestaurant() == null) {
                    System.out.println("[DEBUG] CRITICAL: The menu '" + menu.getTitle() + "' has a NULL restaurant reference!");
                } else {
                    System.out.println("[DEBUG] Menu '" + menu.getTitle() + "' is correctly linked to Restaurant ID: " + menu.getRestaurant().getId());
                }

                menusTitle.add(menu.getTitle());
                List<VendorDto.FoodItemSchemaDTO> items = new ArrayList<>();

                if (menu.getItems() == null) {
                    System.out.println("[DEBUG] CRITICAL: menu.getItems() returned NULL for menu: " + menu.getTitle());
                } else if (menu.getItems().isEmpty()) {
                    System.out.println("[DEBUG] INFO: Menu '" + menu.getTitle() + "' has no items.");
                } else {
                    System.out.println("[DEBUG] Menu '" + menu.getTitle() + "' has " + menu.getItems().size() + " item(s). Looping...");
                    for (Item item : menu.getItems()) {
                        if (item == null) {
                            System.out.println("[DEBUG] CRITICAL: Found a NULL item in the menu '" + menu.getTitle() + "'");
                            continue; // Skip this null item
                        }

                        System.out.println("[DEBUG]   Processing Item -> Name: '" + item.getTitle() + "', ID: " + item.getId());

                        // This is the line that was causing the crash. Let's inspect the object right before we use it.
                        Restaurant itemRestaurant = item.getRestaurant();
                        if (itemRestaurant == null) {
                            System.out.println("[DEBUG]   CRITICAL: item.getRestaurant() returned NULL for item: " + item.getTitle());
                            // We will add a placeholder to avoid a crash, but this identifies the broken data.
                            items.add(new VendorDto.FoodItemSchemaDTO(item.getId(), item.getTitle(), item.getImageBase64(), item.getDescription(), -1, item.getPrice().getPrice(), item.getCount(), item.getHashtags()));
                        } else {
                            System.out.println("[DEBUG]   Item '" + item.getTitle() + "' is correctly linked to Restaurant ID: " + itemRestaurant.getId());
                            items.add(new VendorDto.FoodItemSchemaDTO(item.getId(), item.getTitle(), item.getImageBase64(), item.getDescription(), itemRestaurant.getId(), item.getPrice().getPrice(), item.getCount(), item.getHashtags()));
                        }
                    }
                }
                mapMenuToItems.put(menu.getTitle(), items);
            }
        }

        System.out.println("[DEBUG] --- Finished building menu response. ---");
        return new VendorDto.VendorMenuResponseDTO(mapRestaurantToSchemaDTO(restaurant), menusTitle, mapMenuToItems);
    }
}