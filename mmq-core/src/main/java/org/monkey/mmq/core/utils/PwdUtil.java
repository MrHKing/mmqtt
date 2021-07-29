/**
 * Copyright (c) 2020, Solley (hkk@yanboo.com.cn) All rights reserved.
 */

package org.monkey.mmq.core.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Scanner;

/**
 * 密码
 * @author Solley
 */
public class PwdUtil {

    /**
     * 通过用户名和私钥生成密码
     */
    public static void main(String[] args) throws IOException {
        System.out.println();
        System.out.print("输入需要获取密码的用户名: ");
        Scanner scanner = new Scanner(System.in);
        String value = scanner.nextLine();
        RSAPrivateKey privateKey = IoUtil.readObj(PwdUtil.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
        RSA rsa = new RSA(privateKey, null);
        System.out.println("用户名: " + value + " 对应生成的密码为: " + rsa.encryptBcd(value, KeyType.PrivateKey));
    }

}
