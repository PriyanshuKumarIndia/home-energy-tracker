package com.teamengineoil.user_service.mapper;

import com.teamengineoil.user_service.dto.UpdateRequest;
import com.teamengineoil.user_service.dto.UserDto;
import com.teamengineoil.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserDto toDTO(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public User toEntity(UserDto userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public void updateUserEntity(UpdateRequest userDTO, User user) {
        modelMapper.map(userDTO, user);
    }
}