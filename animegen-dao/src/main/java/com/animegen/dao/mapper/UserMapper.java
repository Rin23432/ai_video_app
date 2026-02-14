package com.animegen.dao.mapper;

import com.animegen.dao.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM `user` WHERE id = #{id}")
    UserDO findById(@Param("id") Long id);
}
