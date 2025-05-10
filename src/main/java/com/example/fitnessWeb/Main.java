package com.example.fitnessWeb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // 입력
        int n = Integer.parseInt(br.readLine());
        System.out.print(fibonacci(n));
    }

    public static BigInteger fibonacci(int n) {
        BigInteger[] arr = new BigInteger[n + 2];
        arr[0] = BigInteger.ZERO;
        arr[1] = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            arr[i] = arr[i - 1].add(arr[i - 2]);
        }

        return arr[n];
    }
}





