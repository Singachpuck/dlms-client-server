package com.dochkas.mandjetDlmsWeb.mapper;


import com.dochkas.mandjetDlmsWeb.model.auth.Authorities;
import com.dochkas.mandjetDlmsWeb.model.dto.UserDto;
import com.dochkas.mandjetDlmsWeb.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        imports = Authorities.class
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User dtoToUser(UserDto userDto);

    @Mapping(target = "password", ignore = true)
    UserDto userToDto(User user);

    default org.springframework.security.core.userdetails.User entityToUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                Authorities.getAllAuthorities());
    }
}
