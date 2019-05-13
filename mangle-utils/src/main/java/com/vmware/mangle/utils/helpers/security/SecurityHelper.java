/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.utils.helpers.security;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;

import com.vmware.mangle.utils.constants.SecurityConstants;
import com.vmware.mangle.utils.exceptions.MangleSecurityException;

/**
 * Security helper Class with Static methods to encrypt and decrypt text using AES algorithm
 * Included methods to generate secrteKey, salt and random salt password
 *
 * @author bkaranam
 */
@Log4j2
public class SecurityHelper {

    private SecurityHelper() {

    }

    private static SecretKey generateSecretKey(String salt, String passwordForSalt) throws MangleSecurityException {
        char[] passwordForSaltArray = null;
        if (null != passwordForSalt) {
            passwordForSaltArray = passwordForSalt.toCharArray();
        }
        PBEKeySpec spec = new PBEKeySpec(passwordForSaltArray, salt.getBytes(),
                SecurityConstants.PASSWORD_ITERATION_COUNT, SecurityConstants.KEY_SIZE_IN_BYTES);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(SecurityConstants.SECRETKEY_FACTORY_ALGORITHM);
            return factory.generateSecret(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new MangleSecurityException(
                    "MangleSecurityException: Generating Secret Key Failed -> RootCause: " + e.getMessage(), e);
        }

    }

    /**
     * Method to encrypt the text
     *
     * @param plainText
     * @param salt
     * @return encrypted text as a string
     * @throws MangleSecurityException
     */

    public static String encrypt(String plainText, String salt) throws MangleSecurityException {
        return encrypt(plainText, salt, null);
    }

    /**
     * Overloaded method to encrypt the text with salt's password
     *
     * @param plainText
     * @param salt
     * @param passwordForSalt
     * @return encrypted text as a string
     * @throws MangleSecurityException
     */

    public static String encrypt(String plainText, String salt, String passwordForSalt) throws MangleSecurityException {
        SecretKeySpec secret = new SecretKeySpec(generateSecretKey(salt, passwordForSalt).getEncoded(),
                SecurityConstants.SECRETKEY_ALGORITHM);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(SecurityConstants.CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, new byte[16]));
            return new Base64().encodeAsString(cipher.doFinal(plainText.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new MangleSecurityException(
                    "MangleSecurityException: Encrypting the Text failed -> RootCause: " + e.getMessage(), e);
        }
    }

    /**
     * Method to encrypt text with default format
     *
     * @param plainText
     * @param salt
     * @param passwordForSalt
     * @return encrypted text as a string
     * @throws MangleSecurityException
     */
    public static String encryptTextinDefaultFormat(String plainText, String salt, String passwordForSalt)
            throws MangleSecurityException {
        String encryptedText = encrypt(plainText, salt, passwordForSalt);
        return String.format(SecurityConstants.STORABLE_ENCRYPTED_TEXT_FORMAT, salt.length(), salt, encryptedText,
                passwordForSalt, passwordForSalt.length());
    }

    /**
     * Method to decrypt text with default format
     *
     * @param encryptedText
     * @return decrypted text as a string
     * @throws MangleSecurityException
     */
    public static String decryptWithDefaultFormat(String encryptedText) throws MangleSecurityException {
        if (!encryptedText.matches(SecurityConstants.STORABLE_ENCRYPTED_TEXT_FORMAT.replaceAll("%d", "\\\\d\\*")
                .replaceAll("%s", ".\\*"))) {
            throw new MangleSecurityException(
                    "Text provided as input cannot be decrypted using Mangle's default format.");
        }
        int firstIndex = encryptedText.indexOf('&') + 1;
        int lastIndex = encryptedText.lastIndexOf('&');
        int lenOfEncryptedText = encryptedText.length();
        int lengthOfSalt = Integer.parseInt(encryptedText.substring(0, firstIndex - 1));
        int lenghtOfSaltPassword = Integer.parseInt(encryptedText.substring(lastIndex + 1, lenOfEncryptedText));
        return decrypt(encryptedText.substring(lengthOfSalt + firstIndex, lastIndex - lenghtOfSaltPassword),
                encryptedText.substring(firstIndex, lengthOfSalt + firstIndex),
                encryptedText.substring(lastIndex - lenghtOfSaltPassword, lastIndex));
    }

    /**
     * Method to decrypt the provided text
     *
     * @param encryptedText
     * @param salt
     * @return decrypted text as a string
     * @throws MangleSecurityException
     */
    public static String decrypt(String encryptedText, String salt) throws MangleSecurityException {
        return decrypt(encryptedText, salt, null);
    }

    /**
     * Overloaded method to decrypt the provided text by providing salt's password
     *
     * @param encryptedText
     * @param salt
     * @param passwordForSalt
     * @return decrypted text as a string
     * @throws MangleSecurityException
     */
    public static String decrypt(String encryptedText, String salt, String passwordForSalt)
            throws MangleSecurityException {
        try {
            byte[] encryptedTextBytes = Base64.decodeBase64(encryptedText);
            SecretKeySpec secret = new SecretKeySpec(generateSecretKey(salt, passwordForSalt).getEncoded(),
                    SecurityConstants.SECRETKEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(SecurityConstants.CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, new byte[16]));
            byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
            return new String(decryptedTextBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new MangleSecurityException(
                    "MangleSecurityException: Decrypting the text failed -> RootCause:" + e.getMessage(), e);
        }
    }

    /**
     * Method to generate salt with default size in bytes
     *
     * @return salt as string
     */
    public static String generateSalt() {
        return generateSalt(SecurityConstants.DEFAULT_SALT_SIZE_IN_BYTES);
    }

    /**
     * Method to generate salt by providing the no of bytes
     *
     * @param noOfBytes
     * @return salt as string
     */
    public static String generateSalt(int noOfBytes) {
        byte[] bytes = new byte[noOfBytes];
        new SecureRandom().nextBytes(bytes);
        return new String(bytes).replaceAll("\r", "").replaceAll("\n", "");
    }

    /**
     * Method to generate random password
     *
     * @param length
     * @return String Generated password as a string
     */
    public static String generateRandomPassword(int length) {
        return RandomStringUtils.random(length, SecurityConstants.CHARACTERS).replaceAll("\r", "").replaceAll("\n", "");
    }

    /**
     * Method to generate random password with default password size
     *
     * @return String Generated password as a string
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(SecurityConstants.DEFAULT_PASSWORD_SIZE);
    }

    public static String generateBase64EncodedTextWithSalt(String textToEncrypt) {
        try {
            String encryptedtext = encryptTextinDefaultFormat(textToEncrypt, SecurityHelper.generateRandomPassword(20),
                    SecurityHelper.generateRandomPassword());
            return new String(Base64.encodeBase64(encryptedtext.getBytes()));
        } catch (MangleSecurityException e) {
            log.error(e);
        }
        return null;
    }
}
