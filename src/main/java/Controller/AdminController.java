package Controller;

import dto.UserDto;
import enums.Role;
import model.User;
import dao.*;

import java.util.ArrayList;
import java.util.List;

public class AdminController {
    CustomerDao customerDao = new CustomerDao();
    DeliverymanDao deliverymanDao = new DeliverymanDao();
    OwnerDao ownerDao = new OwnerDao();

    public List<UserDto.UserSchemaDTO> getAllUsers(User adminUser){
        if (adminUser.getRole() != Role.ADMIN) {
            throw  new SecurityException("Forbidden: Only admins can access this function.");
        }
        List<User> users = new ArrayList<>();
        users.addAll(customerDao.getAll());
        users.addAll(ownerDao.getAll());
        users.addAll(deliverymanDao.getAll());
        UserDto.UserSchemaDTO userSchemaDTO;
        UserDto.RegisterRequestDTO.BankInfoDTO  bankInfoForSchema = null;
        List<UserDto.UserSchemaDTO> usersDTO = new ArrayList<>();
        for(User user : users){
            if (user.getBankName() != null || user.getAccountNumber() != null) {
                bankInfoForSchema = new UserDto.RegisterRequestDTO.BankInfoDTO(user.getBankName(), user.getAccountNumber());
            }
            userSchemaDTO = new UserDto.UserSchemaDTO(
                    user.getPublicId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    bankInfoForSchema
            );
            usersDTO.add(userSchemaDTO);
        }
        return usersDTO;
    }

    public void updateUserApprovalStatus(User adminUser, String userToUpdatePublicId, String newStatus){

    }



    public static class SecurityException extends RuntimeException { public SecurityException(String message) { super(message); } }

}
