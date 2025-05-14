package com.example.fitnessWeb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // 입력
        String[] NM = br.readLine().split(" ");
        int N = Integer.parseInt(NM[0]);
        int M = Integer.parseInt(NM[1]);
        HashMap<Integer, Integer> map = new HashMap<>();

        // x -> y 이동 경로 입력
        for (int i = 0; i < M + N; i++) {
            String[] xy = br.readLine().split(" ");
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            map.put(x, y);
        }

        // 1에서 시작해서 100을 목적지로 bfs
        System.out.print(bfs(1, 100, map));
    }

    public static int bfs(int start, int end, HashMap<Integer, Integer> map) {
        // queue 설정
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{start, 0});


        // visited 설정(100개의 칸 가정)
        boolean[] visited = new boolean[101];
        visited[1] = true;


        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            int currVertex = curr[0];    // 현재 정점 값
            int currWeight = curr[1];   // 현재 가중치

            // 목적지 도착
            if (currVertex == end) return currWeight;


            // 인접 노드 방문
            for (int i = 1; i <= 6; i++) {
                int next = currVertex + i;
                if (next <= 100 && !visited[next]) {
                    while (map.containsKey(next)) {
                        next = map.get(next);
                    }

                    visited[next] = true;
                    queue.add(new int[]{next, currWeight + 1});
                }
            }
        }

        // 목적지 도착 불가
        return -1;
    }
}