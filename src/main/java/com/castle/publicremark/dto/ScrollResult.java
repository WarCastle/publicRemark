package com.castle.publicremark.dto;

import lombok.Data;

import java.util.List;

/**
 * @author YuLong
 * Date: 2022/11/16 16:40
 */
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
