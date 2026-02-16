package com.animegen.dao.mapper;

import com.animegen.dao.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM `user` WHERE id = #{id}")
    UserDO findById(@Param("id") Long id);

    @Select("SELECT * FROM `user` WHERE username = #{username} LIMIT 1")
    UserDO findByUsername(@Param("username") String username);

    @Select("SELECT * FROM `user` WHERE device_id = #{deviceId} LIMIT 1")
    UserDO findByDeviceId(@Param("deviceId") String deviceId);

    @Insert("INSERT INTO `user`(username, password_hash, phone, nickname, avatar_url, bio, role, device_id, created_at, updated_at) " +
            "VALUES(#{username}, #{passwordHash}, #{phone}, #{nickname}, #{avatarUrl}, #{bio}, #{role}, #{deviceId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDO userDO);

    @Update("UPDATE `user` SET nickname = #{nickname}, bio = #{bio}, avatar_url = #{avatarUrl}, updated_at = NOW() WHERE id = #{id}")
    int updateProfile(@Param("id") Long id,
                      @Param("nickname") String nickname,
                      @Param("bio") String bio,
                      @Param("avatarUrl") String avatarUrl);
}
