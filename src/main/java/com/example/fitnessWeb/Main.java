package com.example.fitnessWeb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;


public class Main {
    static class Node implements Comparable<Node> {
        int vertex;
        long weight;

        public Node(int vertex, long weight) {
            this.vertex = vertex;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node o) {
            return Long.compare(this.weight, o.weight);
        }
    }


    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // 입력
        String[] NMKS = br.readLine().split(" ");
        int N = Integer.parseInt(NMKS[0]);  // 정점 수 N
        int M = Integer.parseInt(NMKS[1]);  // 간선 수 M
        int K = Integer.parseInt(NMKS[2]);  // 좀비 점령 도시 수 K
        int S = Integer.parseInt(NMKS[3]);  // 위험한 도시 기준 S

        String[] pq = br.readLine().split(" ");
        int p = Integer.parseInt(pq[0]);
        int q = Integer.parseInt(pq[1]);


        // K개의 좀비에게 점령당한 도시
        ArrayList<Integer> zombieCities = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            zombieCities.add(Integer.parseInt(br.readLine()));
        }


        // M개의 간선 정보
        ArrayList<ArrayList<Integer>> adjList = new ArrayList<>();
        for (int i = 0; i <= N; i++) adjList.add(new ArrayList<>());
        for (int i = 0; i < M; i++) {
            String[] connection = br.readLine().split(" ");
            int v1 = Integer.parseInt(connection[0]);
            int v2 = Integer.parseInt(connection[1]);
            adjList.get(v1).add(v2);
            adjList.get(v2).add(v1);
        }


        // 좀비 도시들로부터 최단 거리 구하기
        long[] zombieDist = getZombieDist(adjList, zombieCities);

        // 결과 값 구하기
        long[] result = dijkstra(adjList, 1, p, q, S, zombieDist);
        System.out.print(result[N]);
    }



    public static long[] getZombieDist(ArrayList<ArrayList<Integer>> adjList, ArrayList<Integer> starts) {
        // dist 배열 초기화
        int N = adjList.size() - 1;
        long[] dist = new long[N + 1];
        Arrays.fill(dist, Long.MAX_VALUE);

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Integer start : starts) {
            dist[start] = 0;    // 시작점 가중치 0
            pq.add(new Node(start, 0)); // 시작점 가중치 0으로 pq에 넣기
        }

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            long currWeight = curr.weight;


            // 최적의 경로가 아님
            if (currWeight > dist[currVertex]) continue;


            // 인접 노드 방문
            for (Integer neighbor : adjList.get(currVertex)) {
                int neighborWeight = 1;
                if (currWeight + neighborWeight < dist[neighbor]) {
                    dist[neighbor] = currWeight + neighborWeight;
                    pq.add(new Node(neighbor, dist[neighbor]));
                }
            }
        }

        return dist;
    }


    public static long[] dijkstra(ArrayList<ArrayList<Integer>> adjList, int start, int p, int q, int S, long[] zombieDist) {
        // dist 배열 초기화
        int N = adjList.size() - 1;
        long[] dist = new long[N + 1];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[start] = 0;    // 시작점 가중치 0

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0)); //


        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            long currWeight = curr.weight;


            // 최적의 경로가 아님
            if (currWeight > dist[currVertex]) continue;


            // 인접 노드 방문
            for (Integer neighbor : adjList.get(currVertex)) {
                if (zombieDist[neighbor] == 0) continue;    // 좀비 감염 도시는 이동 불가
                int neighborWeight = getWeight(zombieDist[neighbor], S, p, q);
                if (neighbor == N) neighborWeight = 0;  // N번 도시는 숙박비 x
                if (currWeight + neighborWeight < dist[neighbor]) {
                    dist[neighbor] = currWeight + neighborWeight;
                    pq.add(new Node(neighbor, dist[neighbor]));
                }
            }
        }

        return dist;
    }

    public static int getWeight(long zombieDist, int S, int p, int q) {
        return (zombieDist <= S) ? q : p;
    }
}











