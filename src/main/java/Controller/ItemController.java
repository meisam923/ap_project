package Controller;

import dao.ItemDao;
import dto.ItemDto;
import model.Item;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemController {

    private final ItemDao itemDao = new ItemDao();

    public List<ItemDto.FoodItemSchemaDTO> listItemsForBuyer(ItemDto.ItemListRequestDTO filterDto){
        String searchTerm = (filterDto != null) ? filterDto.search() : null;
        Integer price = (filterDto != null) ? filterDto.price() : null;
        List<String> keywords = (filterDto != null) ? filterDto.keywords() : null;

        List<Item> items = itemDao.findItems(searchTerm, price, keywords);

        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(this::mapItemToSchemaDTO)
                .collect(Collectors.toList());
    }

    public Optional<ItemDto.FoodItemSchemaDTO> getItemForBuyer(int itemId) {
        try {
            Item item = itemDao.findById(itemId);
            return Optional.ofNullable(mapItemToSchemaDTO(item));
        } catch (Exception e) {
            System.err.println("Error finding item with ID " + itemId + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private ItemDto.FoodItemSchemaDTO mapItemToSchemaDTO(Item item) {
        if (item == null) {
            return null;
        }

        BigDecimal priceValue = new BigDecimal(item.getPrice().getPrice());

        return new ItemDto.FoodItemSchemaDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                priceValue,
                item.getImageBase64()
        );
    }
}