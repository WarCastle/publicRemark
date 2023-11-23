package com.castle.publicremark.utils;

import com.castle.publicremark.dto.UserDTO;

/**
 * @author YuLong
 * Date: 2022/11/16 19:46
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> TL = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        TL.set(user);
    }

    public static UserDTO getUser() {
        return TL.get();
    }

    public static void removeUser() {
        TL.remove();
    }
}
