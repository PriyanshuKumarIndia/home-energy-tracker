package com.teamengineoil.user_service.service;

import com.teamengineoil.user_service.dto.UpdateRequest;
import com.teamengineoil.user_service.dto.UserDto;
import com.teamengineoil.user_service.entity.User;
import com.teamengineoil.user_service.exception.ErrorCode;
import com.teamengineoil.user_service.exception.HomeEnergyTrackerException;
import com.teamengineoil.user_service.mapper.UserMapper;
import com.teamengineoil.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(UserDto userDto) {
        User mappedUser = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(mappedUser);
        return userMapper.toDTO(savedUser);
    }

    public UserDto getUserById(Long id) {
        User mappedUser = userRepository.findById(id).orElseThrow(() -> new HomeEnergyTrackerException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDTO(mappedUser);
    }

    public void updateUser(Long id, UpdateRequest userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new HomeEnergyTrackerException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUserEntity(userDto, user);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)) throw new HomeEnergyTrackerException(ErrorCode.USER_NOT_FOUND);
        userRepository.deleteById(id);
    }
}
