package org.qualitydxb.users;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class Encryption {
    public String encrypt(String data, String secretKey)  {
        try{
            byte[] keyBytes = secretKey.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes); // Encode to Base64
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
