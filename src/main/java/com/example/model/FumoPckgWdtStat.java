package com.example.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * fumo_pckg_wdt_stat
 * @author 2631014
 * @since 2026-11-19 14:22:32
 */
@Data
public class FumoPckgWdtStat implements Serializable {
    /**
     */
    private Long id;

    /**
     */
    private String verId;

    /**
     */
    private String brand;

    /**
     */
    private String pckgNm;

    /**
     */
    private String verAlias;

    /**
     */
    private String pckgSt;

    /**
     */
    private String tgtVer;

    /**
     */
    private String msType;

    /**
     */
    private Integer swCnt;

    /**
     */
    private String swSz;

    /**
     */
    private Integer dvCnt;

    /**
     */
    private String wdtSt;

    /**
     */
    private LocalDate createdAt;

    /**
     */
    private String linkYn;

    /**
     */
    private LocalDateTime linkTime;

    private static final long serialVersionUID = 1L;
}