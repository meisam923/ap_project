package Controller;

import dao.RestaurantDao;
import dto.VendorDto;
import enums.ApprovalStatus;
import enums.OperationalStatus;
import model.Restaurant;
import model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VendorController {

    private final RestaurantDao restaurantDao = new RestaurantDao();

    public List<VendorDto.RestaurantSchemaDTO> listVendorsForBuyer(VendorDto.VendorListRequestDTO filterDto) {
        String searchTerm = (filterDto != null) ? filterDto.search() : null;
        List<String> keywords = (filterDto != null) ? filterDto.keywords() : null;

        List<Restaurant> restaurantEntities = restaurantDao.findVendors(
                searchTerm,
                keywords,
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
                isOpen
        );
    }

    private VendorDto.VendorMenuResponseDTO buildMenuResponseDTO(Restaurant restaurant) {
        // TODO: Implement the actual logic to fetch menus and items
        return new VendorDto.VendorMenuResponseDTO(mapRestaurantToSchemaDTO(restaurant), List.of("Sample Menu"), Collections.emptyMap());
    }
}