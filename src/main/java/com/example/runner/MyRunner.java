package com.example.runner;

import com.example.mapper.FumoPckgWdtStatMapper;
import com.example.model.FumoPckgWdtStat;
import com.example.util.HMap;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyRunner implements CommandLineRunner {
    private final JsonMapper jsonMapper;
    private final FumoPckgWdtStatMapper fumoPckgWdtStatMapper;

    @Override
    public void run(String... args) throws Exception {
        List<HMap> list = fumoPckgWdtStatMapper.selectList();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(e -> log.info("e: {}", e));
        }
//        List<FumoPckgWdtStat> list = fumoPckgWdtStatMapper.selectAll();
//        if (CollectionUtils.isNotEmpty(list)) {
//            list.forEach(e -> log.info("e: {}", jsonMapper.writeValueAsString(e)));
//        }
    }
}
