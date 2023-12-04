package com.castle.publicremark.controller;

import com.castle.publicremark.dto.Result;
import com.castle.publicremark.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author YuLong
 * Date: 2022/11/17 11:43
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    @GetMapping("common/{id}")
    public Result commonFollow(@PathVariable("id") Long followUserId) {
        return followService.commonFollow(followUserId);
    }
}
