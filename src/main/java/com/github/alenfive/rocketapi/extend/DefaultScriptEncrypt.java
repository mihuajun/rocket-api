package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.RocketApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 默认用户授权实现类
 */

@Component
public class DefaultScriptEncrypt implements IScriptEncrypt {

    @Autowired
    private RocketApiProperties properties;

    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    @Override
    public String encrypt(String source) throws Exception {
        if (StringUtils.isEmpty(source)){
            return null;
        }
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(properties.getEncryptKey().getBytes("utf-8"), "AES"));
        try {
            byte[] b = cipher.doFinal(source.getBytes("utf-8"));
            return new String(Base64.getEncoder().encode(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return source;
    }

    @Override
    public String decrypt(String encrypt) throws Exception {
        if (StringUtils.isEmpty(encrypt)){
            return null;
        }
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(properties.getEncryptKey().getBytes("utf-8"), "AES"));

        try {
            byte[] encryptBytes = Base64.getDecoder().decode(encrypt);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            return new String(decryptBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
