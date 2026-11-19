package com.example.mapper;

import com.example.model.FumoPckgWdtStat;
import com.example.util.HMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FumoPckgWdtStatMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FumoPckgWdtStat row);

    int insertSelective(FumoPckgWdtStat row);

    FumoPckgWdtStat selectByPrimaryKey(Long id);

    List<FumoPckgWdtStat> selectAll();

    List<HMap> selectList();

    int updateByPrimaryKeySelective(FumoPckgWdtStat row);

    int updateByPrimaryKey(FumoPckgWdtStat row);
}