package com.uno_restart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uno_restart.types.player.PlayerInfo;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PlayerInfoMapper extends BaseMapper<PlayerInfo> {
    @Select("select password from playerinfo where playerName = #{playerName}")
    String getPasswordByPlayerName(String playerName);

    @Update("update playerinfo set password = #{newPassword} where playerName = #{playerName}")
    void updatePassword(String newPassword, String playerName);
    @Update("update playerinfo set playerName = #{newPlayerName} where playerName = #{playerName}")
    void updatePlayerName(String newPlayerName, String playerName);

    @Update("update playerinfo set avatarPath = #{newAvatarpath} where playerName = #{playerName}")
    void updateAvatarpath(String newAvatarpath, String playerName);

    @Update("update playerinfo set contact = #{contact} where playerName = #{playerName}")
    void updateContact(String contact, String playerName);

    // 传递参数时, 向first参数传递first+2(因为首尾各自多取了一个), 用于判断是否有后驱页, 实际数据不包含第"first + 1"这条记录
    // playerName >= #{after}中的等于条件用于判断是否有前驱页, 实际数据并不包含"playerName = #{after}"的这条记录
    // @Select("select * from playerinfo where playerName like CONCAT(#{playerName}, '%') and playerName >= #{after} limit #{first}")
    @Select("<script>                                           " +
            "   select * from playerinfo                        " +
            "   where playerName like CONCAT(#{playerName}, '%')" +
            "      <if test='after != null and after !=\"\"'>"    +
            "           and playerName >= #{after} "              +
            "      </if>"                                         +
            "   limit #{first}"                                   +
            "</script>")
    List<PlayerInfo> selectPlayerInfoPage(String playerName, Integer first, String after);
}
