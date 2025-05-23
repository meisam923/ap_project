package Controller;


import dao.CartDao;
import lombok.Getter;
import lombok.Setter;
import model.Cart;
import model.Item;

@Getter
@Setter
public class CartController {
    private static CartController instance;

    private final CartDao cartDao = new CartDao();

    private CartController() {
    }
    public static CartController getInstance() {
        if (instance == null) {
            instance = new CartController();
        }
        return instance;
    }

    public void addCart(Cart cart) {
        if (cart == null) {return;}
        cartDao.save(cart);
    }

    

}
