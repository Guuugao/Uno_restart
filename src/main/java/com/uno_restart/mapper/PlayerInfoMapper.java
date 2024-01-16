package com.uno_restart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uno_restart.types.player.PlayerInfo;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface PlayerInfoMapper extends BaseMapper<PlayerInfo> {
    @Select("select password from playerinfo where playerName = #{playerName}")
    String getPasswordByPlayerName(String playerName);

    @Update("update playerinfo set password = #{newPassword} where playerName = #{playerName}")
    void updatePassword(String newPassword, String playerName);
    @Update("update playerinfo set playerName = #{newPlayerName} where playerName = #{playerName}")
    void updatePlayerName(String newPlayerName, String playerName);

    @Update("update playerinfo set avatarPath = #{newAvatarpath} where playerName = #{playerName}")
    void updateAvatarpath(String newAvatarpath, String playerName);
}
