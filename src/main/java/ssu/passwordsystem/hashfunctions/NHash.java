package ssu.passwordsystem.hashfunctions;

import java.io.*;

public class NHash {
    final static int BLOCK_LENGTH = 128;
    final static int BLOCK_LENGTH_CHAR = BLOCK_LENGTH / 8;
    final static int N = 12;

    public static String xor(String s1, String s2) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < s1.length(); i++) {
            ans.append(s1.charAt(i) == s2.charAt(i) ? "0" : "1");
        }
        return ans.toString();
    }

    public static String fillUpTo(String bites, int n) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < n - bites.length(); i++)
            ans.append("0");
        ans.append(bites);
        return ans.toString();
    }

    public static int rotByte(int x, int n) {
        String bites = fillUpTo(Integer.toBinaryString(x), 8);
        String ans = bites.substring(n) + bites.substring(0, n);
        return Integer.parseInt(ans, 2);
    }

    public static int s0(int x1, int x2) {
        return rotByte((x1 + x2) % 256, 2);
    }

    public static int s1(int x1, int x2) {
        return rotByte((x1 + x2 + 1) % 256, 2);
    }

    public static String f(String x, String p) {
        x = xor(x, p);

        int length = x.length();
        int x1 = Integer.parseInt(x.substring(0, length / 4), 2);
        int x2 = Integer.parseInt(x.substring(length / 4, length / 2), 2) ;
        int x3 = Integer.parseInt(x.substring(length / 2, 3 * length / 4), 2) ;
        int x4 = Integer.parseInt(x.substring(3 * length / 4), 2) ;

        x2 = x1 ^ x2;
        x3 = x3 ^ x4;

        x2 = s1(x2, x3);
        x3 = s0(x2, x3);

        x1 = s0(x1, x2);
        x4 = s1(x3, x4);

        return fillUpTo(Integer.toBinaryString(x1), 8)
                + fillUpTo(Integer.toBinaryString(x2), 8)
                + fillUpTo(Integer.toBinaryString(x3), 8)
                + fillUpTo(Integer.toBinaryString(x4), 8);
    }

    public static String ps(String x, String p) {
        int pLength = p.length();
        String p1 = p.substring(0, pLength / 4);
        String p2 = p.substring(pLength / 4, pLength / 2);
        String p3 = p.substring(pLength / 2, 3 * pLength / 4);
        String p4 = p.substring(3 * pLength / 4);

        int xLength = x.length();
        String x1 = x.substring(0, xLength / 4);
        String x2 = x.substring(xLength / 4, xLength / 2);
        String x3 = x.substring(xLength / 2, 3 * xLength / 4);
        String x4 = x.substring(3 * xLength / 4);

        String tmp1 = f(x1, p1);
        tmp1 = xor(tmp1, x2);

        String tmp2 = f(tmp1, p2);
        tmp2 = xor(tmp2, x1);

        x3 = xor(tmp2, x3);
        x4 = xor(tmp1, x4);

        tmp1 = f(x3, p3);
        tmp1 = xor(tmp1, x4);

        tmp2 = f(tmp1, p4);
        tmp2 = xor(tmp2, x3);

        x1 = xor(x1, tmp2);
        x2 = xor(x2, tmp1);

        return x1 + x2 + x3 + x4;
    }

    public static String block(String hPrev, String m) {
        String v = "";
        String o = "";
        for (int i = 0; i < BLOCK_LENGTH / 2; i++) {
            v += "10";
        }
        for (int i = 0; i < BLOCK_LENGTH / 4 - 8; i++) {
            o += "0";
        }

        String tmp = hPrev.substring(BLOCK_LENGTH / 2) + hPrev.substring(0, BLOCK_LENGTH / 2);
        v = xor(tmp, v);
        m = xor(v, m);

        int[] a = new int[4];
        for (int i = 0; i < N; i++) {
            v = "";
            for (int k = 0; k < a.length; k++) {
                a[k] = 4 * i + k + 1;
                v += o + fillUpTo(Integer.toBinaryString(a[k]), 8);
            }
            v = xor(hPrev, v);

            m = ps(m, v);
        }

        return m;
    }

    public static String getFinalBlock(char[] buffer, int n, int length) {
        String ans = "";
        String lengthBites = Integer.toBinaryString(length);
        for (int i = 0; i < n; i++) {
            ans += fillUpTo(Integer.toBinaryString((int)buffer[i]), 8);
        }
        ans += "1";
        for (int i = 0; i < BLOCK_LENGTH - 8 * n - lengthBites.length(); i++) {
            ans += "0";
        }
        ans += lengthBites;
        return ans.substring(0, 128);
    }

    public static String hash(String message) throws IOException {
        CharArrayReader reader = new CharArrayReader(message.toCharArray());

        char[] buffer = new char[BLOCK_LENGTH_CHAR];
        int read = reader.read(buffer);
        if (read < 0) read = 0;

        String hPrev = fillUpTo("", 128);
        String m = "";
        int length = 0;
        while (read >= 0) {
            length += read;
            if (read < BLOCK_LENGTH_CHAR) {
                m = getFinalBlock(buffer, read, length);
            }
            else {
                for (char c: buffer) {
                    m += fillUpTo(Integer.toBinaryString((int)c), 8);
                }
            }
            String h = block(hPrev, m);
            h = xor(h, m);
            hPrev = xor(h, hPrev);
            m = "";

            read = reader.read(buffer);
        }

        StringBuilder hash = new StringBuilder();
        for(int i = 0; i < BLOCK_LENGTH; i += 4) {
            String s = hPrev.substring(i, i + 4);
            hash.append(Integer.toString(Integer.parseInt(s, 2), 16));
        }

        return hash.toString();
    }

    public static void main(String[] args) throws IOException {


        FileWriter fileWriter = new FileWriter("result.txt", false);
        FileReader fileReader = new FileReader("input.txt");

        long start = System.currentTimeMillis();
        char[] buffer = new char[BLOCK_LENGTH_CHAR];
        int read = fileReader.read(buffer);
        if (read < 0) read = 0;

        String hPrev = fillUpTo("", 128);
        String m = "";
        int length = 0;
        while (read >= 0) {
            length += read;
            if (read < BLOCK_LENGTH_CHAR) {
                m = getFinalBlock(buffer, read, length);
            }
            else {
                for (char c: buffer) {
                    m += fillUpTo(Integer.toBinaryString((int)c), 8);
                }
            }
            String h = block(hPrev, m);
            h = xor(h, m);
            hPrev = xor(h, hPrev);
            m = "";

            read = fileReader.read(buffer);
        }

        StringBuilder hash = new StringBuilder();
        for(int i = 0; i < BLOCK_LENGTH; i += 4) {
            String s = hPrev.substring(i, i + 4);
            hash.append(Integer.toString(Integer.parseInt(s, 2), 16));
        }
        long finish = System.currentTimeMillis();

        System.out.println(finish - start);

        fileWriter.write(hash.toString());
        fileWriter.flush();
        fileReader.close();
        fileWriter.close();
    }
}
