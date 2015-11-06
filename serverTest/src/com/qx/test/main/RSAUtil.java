package com.qx.test.main;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;

public class RSAUtil {

	/**
     * 加密
     */
    public static byte[] encrypt(PublicKey pk, byte[] data) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            int blockSize = cipher.getBlockSize();// 获得加密块大小，如：加密前数据为128个byte，而key_size=1024
            // 加密块大小为127
            // byte,加密后为128个byte;因此共有2个加密块，第一个127
            // byte第二个为1个byte
            int outputSize = cipher.getOutputSize(data.length);// 获得加密块加密后块大小
            int leavedSize = data.length % blockSize;
            int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
            byte[] raw = new byte[outputSize * blocksSize];
            int i = 0;
            while (data.length - i * blockSize > 0) {
                if (data.length - i * blockSize > blockSize)
                    cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize);
                else
                    cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
                // 这里面doUpdate方法不可用，查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到
                // ByteArrayOutputStream中，而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了
                // OutputSize所以只好用dofinal方法。
  
                i++;
            }
            return raw;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
  
    /**
     * 解密
     */
    public static byte[] decrypt(PrivateKey pk, byte[] raw) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(cipher.DECRYPT_MODE, pk);
            int blockSize = cipher.getBlockSize();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
            int j = 0;
  
            while (raw.length - j * blockSize > 0) {
                bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
                j++;
            }
            return bout.toByteArray();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
  
    /**
     *
     * 根据本地的RSAKey文件获取KeyPair
     *
     * @throws Exception
     */
    public static KeyPair getKeyPair(String rsaKeyStore) throws Exception {
        FileInputStream fis = new FileInputStream(rsaKeyStore);
        ObjectInputStream oos = new ObjectInputStream(fis);
        KeyPair kp = (KeyPair) oos.readObject();
        oos.close();
        fis.close();
        return kp;
    }
  
    /**
     *
     * 存储KeyPair到本地
     *
     * @throws Exception
     */
    public static void saveKeyPair(KeyPair kp, String path) throws Exception {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        // 生成密钥
        oos.writeObject(kp);
        oos.close();
        fos.close();
    }
  
    /**
     *
     * 用于生成公匙或私匙
     *
     * @throws NoSuchAlgorithmException
     *
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
  
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // 注意密钥大小最好为1024,否则解密会有乱码情况.
        kg.initialize(1024, sr);
        KeyPair genKeyPair = kg.genKeyPair();
        return genKeyPair;
  
    }
  //随机生成字段
  	public static String getRandomString(int length) { 
  		String base = "abcdefghijklmnopqrstuvwxyz0123456789俞伯牙席潮海丁克曾管正学管虎管谟业管仲陈伟霆王世充李渊杨坚郭树清李鸿忠王穗明刘铁男李登辉彭长健邓鸿王中军景百孚赵永亮陆兆禧严介和郁亮茅于轼王小波冯唐";   
  	    Random random = new Random();   
  	    StringBuffer sb = new StringBuffer();   
  	    for (int i = 0; i < length; i++) {   
  	        int number = random.nextInt(base.length());   
  	        sb.append(base.charAt(number));   
  	    }   
  	    return sb.toString();   
  	 }  
    /**
     *
     * 测试
     *
     */
    /**
     * @Description: TODO
     * @param args
     * @throws Exception
     */
  	public static void main(String[] args) throws Exception {

  		// 获取公匙及私匙
  		//        KeyPair generateKeyPair = getKeyPair("E:\\key");
  		//生成公钥及私钥
  		KeyPair generateKeyPair = generateKeyPair();

  		// 公匙 用于前台加密
  		PublicKey publicKey = generateKeyPair.getPublic();
  		System.out.println(publicKey);

  		// 私匙 存储在后台用于解密
  		PrivateKey privateKey = generateKeyPair.getPrivate();
  		System.out.println(privateKey);

  		// 存储KeyPair到本地用于后期解密 注意修改前台RSAKeyPair
  		//saveKeyPair(generateKeyPair,"E:\\code\\key");

  		// 测试加密解密

  		// test = "阿斯顿发送对发生地发送盗伐水电费圣达菲sadfsadf爱上对方爱上对方";
  		long   startTime0 = System.currentTimeMillis();
  		for (int i = 0; i < 10; i++) {
  			String test = getRandomString(5);
  			System.out.println("当前字符:" + test);
  			long   startTime = System.currentTimeMillis();
  			byte[] en_test = encrypt(publicKey, test.getBytes());
  			long cur1 = System.currentTimeMillis();
  			System.out.println("加密后字符:" + new String(en_test));
  			System.out.println("加密时间:"+(cur1 - startTime));
  			byte[] de_test = decrypt(privateKey, en_test);
  			long cur2 = System.currentTimeMillis();
  			System.out.println("解密后字符:" + new String(de_test));
  			System.out.println("解密时间:"+(cur2 - startTime));
//  			Thread.sleep(1000*1);
  		}
  		long cur3 = System.currentTimeMillis();
  		System.out.println("共用时间:"+(cur3 - startTime0));


  	}
}
