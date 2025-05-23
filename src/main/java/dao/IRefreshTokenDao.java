package dao;
import model.RefreshToken;
import model.User;

public interface IRefreshTokenDao extends IDao<RefreshToken , Long> {
    RefreshToken findByToken(String token);
    void deleteByUser(User user);
}
