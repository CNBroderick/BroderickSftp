package org.bklab.sftp;

import org.bklab.clevertree.dto.SecurityData;
import org.bklab.clevertree.exception.SecurityException;
import org.bklab.clevertree.security.SecurityHelper;
import org.bklab.ssh2.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class SftpTest {
    @Test
    public void loginRemoteSftpServer() {
        String host = "broderick.cn";
        String user = "root";
        String pswd = "";
        File publicKey = new File("E:\\Broderick_ECS_Qingdao.pem");
        int port = 62;
        SFTPv3Client client;

        Map<SFTPv3DirectoryEntry, Vector<SFTPv3DirectoryEntry>> temp = new HashMap<>();

        try {
            Connection connection = new Connection(host, port);
            connection.connect();
            boolean isAuthenticated = connection.authenticateWithPublicKey(user, publicKey, pswd);
            if (!isAuthenticated) {
                throw new IOException("Authentication failed");
            } else {
                client = new SFTPv3Client(connection);
                System.out.println("Successfully pass authentication to login remote SFTP server: " + host);
            }
            String path = isWindowsOS() ? Arrays.asList(File.listRoots()).get(0).getAbsolutePath() :"/";
            Vector<SFTPv3DirectoryEntry> ls = client.ls(path);
            ls.forEach(entry -> {
                try {
                    if (entry.attributes.isDirectory())
                        temp.put(entry, client.ls(path + File.separator + entry.filename));
                    else temp.put(entry, null);
                } catch (IOException ignore) {}
            });



        } catch (Exception ignore) {
        }
    }

    @Test
    public void testEncrypt() {
        SecurityData data = new SecurityData("bklab.org", "Broderick Labs");
        String base64String;
        SecurityHelper helper;
        try {
            base64String = Base64.getEncoder().encodeToString("蓝鹰安行".getBytes("UTF-8"));
            System.out.println("Base64 encode result: " + base64String);
            helper = new SecurityHelper(data);
            byte[] encrypted = helper.encrypt(base64String.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encrypted.length; i++) {
                String temp = Integer.toHexString(encrypted[i] & 0xFF);
                if (temp.length() == 1) temp = "0" + temp;
                sb.append(temp + " ");
                if ((i + 1) % 8 == 0) sb.append(" ");
                if ((i + 1) % 16 == 0) sb.append("\n");
            }
            System.out.printf("After Encrypted Length: %d bit, data:\n%s%n", encrypted.length, sb.toString());
            byte[] decodeAesData = helper.decrypt(encrypted);
            String decrypted = new String(Base64.getDecoder().decode(decodeAesData), "UTF-8");
            System.out.println("After Decrypted：" + decrypted);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsWindows(){
        List<File> list = Arrays.asList(File.listRoots());
        List<File> root = Arrays.asList(new File(list.get(0).getAbsolutePath()).listFiles());
        root.forEach(file -> System.out.println(file.getName()));

    }


    private static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
