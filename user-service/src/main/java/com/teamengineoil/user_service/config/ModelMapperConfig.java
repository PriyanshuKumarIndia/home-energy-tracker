package com.teamengineoil.user_service.config;

import com.teamengineoil.user_service.dto.UserDto;
import com.teamengineoil.user_service.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        // Entity -> DTO
//        modelMapper.typeMap(User.class, UserDto.class)
//                .addMappings(mapper ->
//                        mapper.skip(UserDto::setPassword)
//                );

        // DTO -> Entity
        modelMapper.typeMap(UserDto.class, User.class)
                .addMappings(mapper ->
                        mapper.skip(User::setId)
                );

        return modelMapper;
    }
}