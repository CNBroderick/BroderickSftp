package org.bklab.sftp.utils;

import java.io.*;
import java.text.ParseException;

/**
 * @author Broderick
 */
public class WindowsShortcut {
    private boolean isDirectory;
    private boolean isLocal;
    private String real_file;

    public WindowsShortcut(File file) throws IOException, ParseException {
        InputStream in = new FileInputStream(file);
        try {
            parseLink(getBytes(in));
        } finally {
            in.close();
        }
    }

    public static boolean isPotentialValidLink(File file) {
        final int minimum_length = 0x64;
        boolean isPotentiallyValid = false;
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            isPotentiallyValid = file.isFile()
                    && file.getName().toLowerCase().endsWith(".lnk")
                    && fis.available() >= minimum_length
                    && isMagicPresent(getBytes(fis, 32));
        } catch (IOException e) {
            isPotentiallyValid = false;
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
        return isPotentiallyValid;
    }

    private static byte[] getBytes(InputStream in) throws IOException {
        return getBytes(in, null);
    }

    private static byte[] getBytes(InputStream in, Integer max) throws IOException {
        // read the entire file into a byte buffer
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (max == null || max > 0) {
            int n = in.read(buff);
            if (n == -1) {
                break;
            }
            bout.write(buff, 0, n);
            if (max != null)
                max -= n;
        }
        in.close();
        return bout.toByteArray();
    }

    private static boolean isMagicPresent(byte[] link) {
        final int magic = 0x0000004C;
        final int magic_offset = 0x00;
        return link.length >= 32 && bytesToDword(link, magic_offset) == magic;
    }

    private static String getNullDelimitedString(byte[] bytes, int off) {
        int len = 0;
        // count bytes until the null character (0)
        while (true) {
            if (bytes[off + len] == 0) {
                break;
            }
            len++;
        }
        return new String(bytes, off, len);
    }

    private static int bytesToWord(byte[] bytes, int off) {
        return ((bytes[off + 1] & 0xff) << 8) | (bytes[off] & 0xff);
    }

    private static int bytesToDword(byte[] bytes, int off) {
        return (bytesToWord(bytes, off + 2) << 16) | bytesToWord(bytes, off);
    }


    public String getRealFilename() {
        return real_file;
    }


    public boolean isLocal() {
        return isLocal;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    private void parseLink(byte[] link) throws ParseException {
        try {
            if (!isMagicPresent(link))
                throw new ParseException("Invalid shortcut; magic is missing", 0);

            byte flags = link[0x14];

            final int file_atts_offset = 0x18;
            byte file_atts = link[file_atts_offset];
            byte is_dir_mask = (byte) 0x10;
            if ((file_atts & is_dir_mask) > 0) {
                isDirectory = true;
            } else {
                isDirectory = false;
            }

            final int shell_offset = 0x4c;
            final byte has_shell_mask = (byte) 0x01;
            int shell_len = 0;
            if ((flags & has_shell_mask) > 0) {
                shell_len = bytesToWord(link, shell_offset) + 2;
            }

            int file_start = 0x4c + shell_len;

            final int file_location_info_flag_offset_offset = 0x08;
            int file_location_info_flag = link[file_start + file_location_info_flag_offset_offset];
            isLocal = (file_location_info_flag & 2) == 0;
            final int basename_offset_offset = 0x10;
            final int networkVolumeTable_offset_offset = 0x14;
            final int finalname_offset_offset = 0x18;
            int finalname_offset = link[file_start + finalname_offset_offset] + file_start;
            String finalname = getNullDelimitedString(link, finalname_offset);
            if (isLocal) {
                int basename_offset = link[file_start + basename_offset_offset] + file_start;
                String basename = getNullDelimitedString(link, basename_offset);
                real_file = basename + finalname;
            } else {
                int networkVolumeTable_offset = link[file_start + networkVolumeTable_offset_offset] + file_start;
                int shareName_offset_offset = 0x08;
                int shareName_offset = link[networkVolumeTable_offset + shareName_offset_offset]
                        + networkVolumeTable_offset;
                String shareName = getNullDelimitedString(link, shareName_offset);
                real_file = shareName + "\\" + finalname;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Could not be parsed, probably not a valid WindowsShortcut", 0);
        }
    }

    public File getResolvedFile() {
        return new File(real_file);
    }

}
